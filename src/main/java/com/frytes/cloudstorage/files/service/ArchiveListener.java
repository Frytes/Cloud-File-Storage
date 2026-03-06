package com.frytes.cloudstorage.files.service;

import com.frytes.cloudstorage.config.RabbitMQConfig;
import com.frytes.cloudstorage.files.dto.ArchiveTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArchiveListener {


    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void listen(ArchiveTask task) {
        log.info("🐇 ЗАДАЧА ПОЛУЧЕНА: ID тикета = {}, Пользователь = {}",
                task.ticketId(), task.userId());

        try {
            log.info("⏳ Начинаю архивацию для пути: {}", task.path());
            Thread.sleep(2000);
            log.info("✅ Архивация завершена: {}", task.ticketId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Архивация прервана", e);
        }
    }
}