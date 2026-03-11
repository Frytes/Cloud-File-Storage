package com.frytes.cloudstorage.files.service;

import com.frytes.cloudstorage.common.exception.ResourceNotFoundException;
import com.frytes.cloudstorage.common.exception.StorageOperationException;
import com.frytes.cloudstorage.common.util.PathUtils;
import com.frytes.cloudstorage.config.RabbitMQConfig;
import com.frytes.cloudstorage.files.dto.ArchiveStatus;
import com.frytes.cloudstorage.files.dto.ArchiveTask;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArchiveService {

    private final RabbitTemplate rabbitTemplate;
    private final StringRedisTemplate redisTemplate;
    private final MinioService minioService;

    @Value("${app.archive.expiration-hours:24}")
    private long expirationHours;

    public String sendArchivingTask(Long userId, String username, String path, Long totalSize) {
        String ticketId = UUID.randomUUID().toString();

        String redisKey = "archive:status:" + ticketId;
        redisTemplate.opsForValue().set(redisKey, ArchiveStatus.IN_PROGRESS.name(), 24, TimeUnit.HOURS);

        ArchiveTask task = ArchiveTask.builder()
                .ticketId(ticketId)
                .userId(userId)
                .username(username)
                .path(path)
                .totalSize(totalSize)
                .build();

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY,
                task
        );

        log.info("Task sent to RabbitMQ: ticket={}, user={}, path={}", ticketId, userId, path);
        return ticketId;
    }

    public Map<String, String> getArchiveStatus(String ticket, Long userId) {
        String redisKey = "archive:status:" + ticket;
        String statusStr = redisTemplate.opsForValue().get(redisKey);

        if (statusStr == null) {
            throw new ResourceNotFoundException("Тикет не найден или истек");
        }

        ArchiveStatus status;
        try {
            status = ArchiveStatus.valueOf(statusStr);
        } catch (IllegalArgumentException e) {
            status = ArchiveStatus.ERROR;
        }

        if (status == ArchiveStatus.READY) {
            String archiveName = "user-" + userId + "-files/archive-" + ticket + ".zip";
            String url = minioService.getPresignedUrl(archiveName);
            return status.toResponse(url, expirationHours * 3600);
        }

        return status.toResponse(null, null);
    }

    public StreamingResponseBody createSyncZipStream(Long userId, String path) {
        String prefix = PathUtils.ensureTrailingSlash(PathUtils.buildUserPath(userId, PathUtils.sanitize(path)));

        return outputStream -> {
            try (ZipOutputStream zipOut = new ZipOutputStream(outputStream)) {
                Iterable<Result<Item>> results = minioService.listObjectsRecursive(prefix);

                for (Result<Item> result : results) {
                    Item item = result.get();
                    if (item.isDir()) {
                        continue;
                    }

                    String objectName = item.objectName();
                    String entryName = objectName.substring(prefix.length());

                    zipOut.putNextEntry(new ZipEntry(entryName));
                    try (InputStream fileStream = minioService.getFile(objectName)) {
                        fileStream.transferTo(zipOut);
                    }
                    zipOut.closeEntry();
                }
            } catch (Exception e) {
                log.error("Ошибка при синхронном создании ZIP", e);
                throw new StorageOperationException("Не удалось создать архив " + path, e);
            }
        };
    }
}
