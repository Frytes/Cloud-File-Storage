package com.frytes.cloudstorage.files.repository.minio;

import com.frytes.cloudstorage.common.exception.DirectoryCreationException;
import com.frytes.cloudstorage.common.exception.FileUploadException;
import com.frytes.cloudstorage.common.exception.StorageOperationException;
import com.frytes.cloudstorage.config.properties.MinioProperties;
import com.frytes.cloudstorage.files.repository.UserStorageWriter;
import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MinioUserStorageWriter implements UserStorageWriter {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;


    @Override
    public void createDirectory(String path) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.buckets().userFiles())
                            .object(path)
                            .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to create directory in MinIO. Path: {}", path, e);
            throw new DirectoryCreationException("Ошибка при создании папки", e);
        }
    }

    @Override
    public void uploadFile(String path, InputStream inputStream, String contentType) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.buckets().userFiles())
                            .object(path)
                            .stream(inputStream, -1, minioProperties.streamPartSize())
                            .contentType(contentType)
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to upload file to MinIO. Path: {}", path, e);
            throw new FileUploadException("Ошибка загрузки файла", e);
        }
    }

    @Override
    public void removeObject(String path) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioProperties.buckets().userFiles())
                            .object(path)
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to remove object from MinIO. Path: {}", path, e);
            throw new StorageOperationException("Ошибка при удалении файла", e);
        }
    }

    @Override
    public void removeObjects(List<String> paths) {
        try {
            List<DeleteObject> objects = paths.stream()
                    .map(DeleteObject::new)
                    .toList();

            Iterable<Result<DeleteError>> results = minioClient.removeObjects(
                    RemoveObjectsArgs.builder()
                            .bucket(minioProperties.buckets().userFiles())
                            .objects(objects)
                            .build()
            );

            for (Result<DeleteError> result : results) {
                DeleteError error = result.get();
                log.error("Failed to delete object in batch. Object: {}, Error: {}", error.objectName(), error.message());
            }
        } catch (Exception e) {
            log.error("Failed to execute batch remove in MinIO.", e);
            throw new StorageOperationException("Ошибка при массовом удалении файлов", e);
        }
    }

    @Override
    public void copyObject(String source, String target) {
        try {
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(minioProperties.buckets().userFiles())
                            .source(CopySource.builder().bucket(minioProperties.buckets().userFiles()).object(source).build())
                            .object(target)
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to copy object in MinIO. Source: {}, Target: {}", source, target, e);
            throw new StorageOperationException("Ошибка копирования файла", e);
        }
    }
}
