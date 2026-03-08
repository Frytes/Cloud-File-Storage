package com.frytes.cloudstorage.files.service;

import com.frytes.cloudstorage.common.exception.ArchiveCreationException;
import com.frytes.cloudstorage.common.util.PathUtils;
import com.frytes.cloudstorage.config.RabbitMQConfig;
import com.frytes.cloudstorage.files.dto.ArchiveStatus;
import com.frytes.cloudstorage.files.dto.ArchiveTask;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArchiveListener {

    private final MinioService minioService;
    private final StringRedisTemplate redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void listen(ArchiveTask task) {
        log.info("📦 Начало архивации: {}", task);

        String redisKey = "archive:status:" + task.ticketId();
        redisTemplate.expire(redisKey, 24, TimeUnit.HOURS);

        String archiveName = "archive-" + task.ticketId() + ".zip";
        String sourcePrefix = PathUtils.buildUserPath(task.userId(), PathUtils.sanitize(task.path()));
        String finalSourcePrefix = PathUtils.ensureTrailingSlash(sourcePrefix);

        try (PipedInputStream pipedIn = new PipedInputStream();
             PipedOutputStream pipedOut = new PipedOutputStream(pipedIn)) {

            CompletableFuture<Void> zipFuture = CompletableFuture.runAsync(() -> {
                try (ZipOutputStream zipOut = new ZipOutputStream(pipedOut)) {
                    Iterable<Result<Item>> results = minioService.listObjectsRecursive(finalSourcePrefix);
                    for (Result<Item> result : results) {
                        Item item = result.get();
                        if (item.isDir()) continue;

                        String objectName = item.objectName();
                        String entryName = objectName.substring(finalSourcePrefix.length());

                        ZipEntry zipEntry = new ZipEntry(entryName);
                        zipOut.putNextEntry(zipEntry);

                        try (InputStream fileStream = minioService.getFile(objectName)) {
                            fileStream.transferTo(zipOut);
                        }
                        zipOut.closeEntry();
                    }
                } catch (Exception e) {
                    throw new ArchiveCreationException("Failed to zip", e);
                }
            });

            minioService.uploadArchive(archiveName, pipedIn);

            zipFuture.join();

            log.info("✅ Архив готов: {}", archiveName);

            redisTemplate.opsForValue().set(redisKey, ArchiveStatus.READY.name(), 24, TimeUnit.HOURS);

            String downloadUrl = minioService.getPresignedUrl(archiveName);

            messagingTemplate.convertAndSendToUser(
                    task.username(),
                    "/queue/archive",
                    Map.of(
                            ArchiveStatus.STATUS_KEY, ArchiveStatus.READY.name(),
                            "ticket", task.ticketId(),
                            "url", downloadUrl
                    )
            );

        } catch (Exception e) {
            log.error("❌ Ошибка при архивации", e);
            
            redisTemplate.opsForValue().set(redisKey, ArchiveStatus.ERROR.name(), 24, TimeUnit.HOURS);

            messagingTemplate.convertAndSendToUser(
                    task.username(),
                    "/queue/archive",
                    Map.of(
                            ArchiveStatus.STATUS_KEY, ArchiveStatus.ERROR.name(),
                            "message", "Ошибка при создании архива"
                    )
            );
        }
    }
}