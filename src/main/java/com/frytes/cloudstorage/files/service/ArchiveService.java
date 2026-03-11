package com.frytes.cloudstorage.files.service;

import com.frytes.cloudstorage.common.exception.ResourceNotFoundException;
import com.frytes.cloudstorage.config.RabbitMQConfig;
import com.frytes.cloudstorage.files.dto.ArchiveStatus;
import com.frytes.cloudstorage.files.dto.ArchiveTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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

    public Map<String, String> getArchiveStatus(String ticket) {
        String redisKey = "archive:status:" + ticket;
        String statusStr  = redisTemplate.opsForValue().get(redisKey);

        if (statusStr  == null) {
            throw new ResourceNotFoundException("Тикет не найден или истек");
        }
        ArchiveStatus status;
        try {
            status = ArchiveStatus.valueOf(statusStr);
        } catch (IllegalArgumentException e) {
            status = ArchiveStatus.ERROR;
        }

        if (status == ArchiveStatus.READY) {
            String archiveName = "archive-" + ticket + ".zip";
            String url = minioService.getPresignedUrl(archiveName);
            return status.toResponse(url, expirationHours * 3600);
        }
        return status.toResponse(null, null);
    }
}
