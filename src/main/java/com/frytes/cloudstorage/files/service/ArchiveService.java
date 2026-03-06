package com.frytes.cloudstorage.files.service;

import com.frytes.cloudstorage.config.RabbitMQConfig;
import com.frytes.cloudstorage.files.dto.ArchiveTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArchiveService {

    private final RabbitTemplate rabbitTemplate;

    public String sendArchivingTask(Long userId, String path, Long totalSize) {
        String ticketId = UUID.randomUUID().toString();

        ArchiveTask task = ArchiveTask.builder()
                .ticketId(ticketId)
                .userId(userId)
                .path(path)
                .totalSize(totalSize)
                .build();

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY,
                task
        );

        log.info("Task sent to RabbitMQ: ticket={}, user={}, path={}", ticketId, userId, path);

        // TODO  Redis статус "IN_PROGRESS"

        return ticketId;
    }
}