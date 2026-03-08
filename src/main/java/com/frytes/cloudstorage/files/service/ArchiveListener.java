package com.frytes.cloudstorage.files.service;

import com.frytes.cloudstorage.common.exception.ArchiveCreationException;
import com.frytes.cloudstorage.config.RabbitMQConfig;
import com.frytes.cloudstorage.files.dto.ArchiveTask;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArchiveListener {

    private final MinioService minioService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void listen(ArchiveTask task) {

        try (PipedInputStream pipedIn = new PipedInputStream();
             PipedOutputStream pipedOut = new PipedOutputStream(pipedIn)) {

            CompletableFuture<Void> uploadFuture = CompletableFuture.runAsync(() -> {
                try (ZipOutputStream zipOut = new ZipOutputStream(pipedOut)) {
                    Iterable<Result<Item>> results = minioService.listObjectsRecursive(task.path());

                    for (Result<Item> result : results) {
                        Item item = result.get();
                        if (item.isDir()) {
                            continue;
                        }

                        String fileName = item.objectName();
                        log.debug("Adding file to archive: {}", fileName);

                        ZipEntry zipEntry = new ZipEntry(fileName);
                        zipOut.putNextEntry(zipEntry);

                        try (InputStream fileStream = minioService.getFile(fileName)) {
                            fileStream.transferTo(zipOut);
                        }
                        zipOut.closeEntry();
                    }
                } catch (Exception e) {
                    throw new ArchiveCreationException("Failed to create archive for task: " + task.ticketId(), e);
                }
            });

            String archiveName = "archive-" + task.ticketId() + ".zip";
            minioService.upload(archiveName, pipedIn, "application/zip");
            uploadFuture.join();

            log.info("✅ Архив успешно создан и загружен: {}", archiveName);

        } catch (Exception e) {
            log.error("❌ Критическая ошибка при архивации", e);
        }
    }
}