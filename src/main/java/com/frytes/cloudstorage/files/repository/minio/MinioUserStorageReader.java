package com.frytes.cloudstorage.files.repository.minio;

import com.frytes.cloudstorage.common.exception.FileUploadException;
import com.frytes.cloudstorage.common.exception.StorageOperationException;
import com.frytes.cloudstorage.config.properties.MinioProperties;
import com.frytes.cloudstorage.files.model.StorageItem;
import com.frytes.cloudstorage.files.model.StorageMetadata;
import com.frytes.cloudstorage.files.repository.UserStorageReader;
import io.minio.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MinioUserStorageReader implements UserStorageReader {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    @Override
    public InputStream getFile(String path) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioProperties.buckets().userFiles())
                            .object(path)
                            .build()
            );
        } catch (Exception e) {
            throw new FileUploadException("Ошибка при скачивании файла: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isObjectExist(String path) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(minioProperties.buckets().userFiles())
                            .object(path)
                            .build()
            );
            return true;
        } catch (io.minio.errors.ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code())) {
                return false;
            }
            throw new StorageOperationException("Ошибка при проверке объекта", e);
        } catch (Exception e) {
            throw new StorageOperationException("Ошибка при проверке объекта", e);
        }
    }

    @Override
    public StorageMetadata getMetadata(String path) {
        try {
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(minioProperties.buckets().userFiles())
                            .object(path)
                            .build()
            );
            return new StorageMetadata(
                    stat.size(),
                    stat.lastModified().toLocalDateTime()
            );
        } catch (Exception e) {
            throw new StorageOperationException("Ошибка получения метаданных", e);
        }
    }

    @Override
    public List<StorageItem> listObjects(String prefix, boolean recursive) {
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(minioProperties.buckets().userFiles())
                        .prefix(prefix)
                        .recursive(recursive)
                        .build()
        );

        List<StorageItem> items = new ArrayList<>();
        for (Result<Item> result : results) {
            try {
                Item item = result.get();
                LocalDateTime lastModified = null;
                long size = 0L;

                if (!item.isDir()) {
                    if (item.lastModified() != null) {
                        lastModified = item.lastModified().toLocalDateTime();
                    }
                    size = item.size();
                }
                items.add(new StorageItem(item.objectName(), item.isDir(), size, lastModified));
            } catch (Exception e) {
                log.error("MinIO error during listObjects with prefix {}: {}", prefix, e.getMessage());
                throw new StorageOperationException("Ошибка чтения списка файлов", e);
            }
        }
        return items;
    }
}
