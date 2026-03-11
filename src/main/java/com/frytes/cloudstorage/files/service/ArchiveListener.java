package com.frytes.cloudstorage.files.service;

import com.frytes.cloudstorage.common.exception.ArchiveCreationException;
import com.frytes.cloudstorage.common.util.PathUtils;
import com.frytes.cloudstorage.config.RabbitMQConfig;
import com.frytes.cloudstorage.config.properties.AppProperties;
import com.frytes.cloudstorage.files.dto.ArchiveStatus;
import com.frytes.cloudstorage.files.dto.ArchiveTask;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Component
public class ArchiveListener {

    private final MinioService minioService;
    private final StringRedisTemplate redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final TaskExecutor applicationTaskExecutor;
    private final AppProperties appProperties;

    private static final int PIPE_BUFFER_SIZE = 5 * 1024 * 1024;
    private static final int TIMEOUT = 10;

    public ArchiveListener(MinioService minioService,
                           StringRedisTemplate redisTemplate,
                           SimpMessagingTemplate messagingTemplate,
                           @Qualifier("archiveTaskExecutor") TaskExecutor applicationTaskExecutor,
                           AppProperties appProperties) {
        this.minioService = minioService;
        this.redisTemplate = redisTemplate;
        this.messagingTemplate = messagingTemplate;
        this.applicationTaskExecutor = applicationTaskExecutor;
        this.appProperties = appProperties;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void listen(ArchiveTask task) {
        log.info("📦 [Ticket: {}] Начало архивации", task.ticketId());

        String redisKey = "archive:status:" + task.ticketId();
        redisTemplate.expire(redisKey, appProperties.archive().expirationHours(), TimeUnit.HOURS);

        String archiveName = "user-" + task.userId() + "-files/archive-" + task.ticketId() + ".zip";

        String sourcePrefix = PathUtils.ensureTrailingSlash(
                PathUtils.buildUserPath(task.userId(), PathUtils.sanitize(task.path()))
        );

        try (PipedInputStream pipedIn = new PipedInputStream(PIPE_BUFFER_SIZE);
             PipedOutputStream pipedOut = new PipedOutputStream(pipedIn)) {

            CompletableFuture<Void> zipFuture = CompletableFuture.runAsync(() -> {
                try (ZipOutputStream zipOut = new ZipOutputStream(pipedOut)) {
                    zipOut.setLevel(Deflater.NO_COMPRESSION);
                    Iterable<Result<Item>> results = minioService.listObjectsRecursive(sourcePrefix);

                    for (Result<Item> result : results) {
                        Item item = result.get();
                        if (item.isDir()) continue;

                        String objectName = item.objectName();
                        String entryName = objectName.substring(sourcePrefix.length());

                        zipOut.putNextEntry(new ZipEntry(entryName));
                        try (InputStream fileStream = minioService.getFile(objectName)) {
                            fileStream.transferTo(zipOut);
                        }
                        zipOut.closeEntry();
                    }
                } catch (Exception e) {
                    throw new ArchiveCreationException("Failed to zip", e);
                }
            }, applicationTaskExecutor);

            minioService.uploadArchive(archiveName, pipedIn);
            zipFuture.get(TIMEOUT, TimeUnit.MINUTES);

            log.info("✅ [Ticket: {}] Архив успешно загружен", task.ticketId());
            redisTemplate.opsForValue().set(redisKey, ArchiveStatus.READY.name(), appProperties.archive().expirationHours(), TimeUnit.HOURS);

            String downloadUrl = minioService.getPresignedUrl(archiveName);
            sendWebSocketMessage(task, ArchiveStatus.READY.name(), "downloadUrl", downloadUrl);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("❌ [Ticket: {}] Архивация прервана", task.ticketId(), e);
            handleError(task, redisKey);
        } catch (TimeoutException e) {
            log.error("❌ [Ticket: {}] Превышено время ожидания", task.ticketId(), e);
            handleError(task, redisKey);
        } catch (Exception e) {
            log.error("❌ [Ticket: {}] Ошибка при архивации", task.ticketId(), e);
            handleError(task, redisKey);
        }
    }


    private void handleError(ArchiveTask task, String redisKey) {
        redisTemplate.opsForValue().set(redisKey, ArchiveStatus.ERROR.name(), 24, TimeUnit.HOURS);
        sendWebSocketMessage(task, ArchiveStatus.ERROR.name(), "message", "Ошибка при создании архива");
    }

    private void sendWebSocketMessage(ArchiveTask task, String status, String key, String value) {
        messagingTemplate.convertAndSendToUser(
                task.username(),
                "/queue/archive",
                Map.of(ArchiveStatus.STATUS_KEY, status, key, value, "ticket", task.ticketId())
        );
    }
}