package com.frytes.cloudstorage.files.service;

import com.frytes.cloudstorage.common.exception.DirectoryCreationException;
import com.frytes.cloudstorage.common.exception.FileUploadException;
import com.frytes.cloudstorage.common.exception.StorageOperationException;
import io.minio.*;
import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.buckets.user-files}")
    private String userFilesBucket;

    @PostConstruct
    public void init() {
        createBucketIfNotExists(userFilesBucket);
    }
    public void createDirectory(String objectName) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(userFilesBucket)
                            .object(objectName)
                            .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                            .build()
            );
        } catch (Exception e) {
            throw new DirectoryCreationException("Ошибка при создании папки: " + e.getMessage(), e);
        }
    }

    public void upload(String objectName, InputStream inputStream, String contentType) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(userFilesBucket)
                            .object(objectName)
                            .stream(inputStream, -1, 10485760)
                            .contentType(contentType)
                            .build()
            );
        } catch (Exception e) {
            throw new FileUploadException("Ошибка загрузки файла в MinIO: " + e.getMessage(), e);
        }
    }

    public Iterable<Result<Item>> listObjects(String prefix) {
        ListObjectsArgs args = ListObjectsArgs.builder()
                .bucket(userFilesBucket)
                .prefix(prefix)
                .recursive(false)
                .build();

        return minioClient.listObjects(args);
    }

    public InputStream getFile(String objectName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(userFilesBucket)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            throw new FileUploadException("Ошибка при скачивании файла: " + e.getMessage(), e);
        }
    }


    public void removeObject(String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(userFilesBucket)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            throw new StorageOperationException("Ошибка при удалении файла: " + e.getMessage(), e);
        }
    }

    public void copyObject(String source, String target) {
        try {
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(userFilesBucket)
                            .source(CopySource.builder().bucket(userFilesBucket).object(source).build())
                            .object(target)
                            .build()
            );
        } catch (Exception e) {
            throw new StorageOperationException("Ошибка копирования файла: " + source, e);
        }
    }


    public Iterable<Result<Item>> listObjectsRecursive(String prefix) {
        return minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(userFilesBucket)
                        .prefix(prefix)
                        .recursive(true)
                        .build()
        );
    }

    private void createBucketIfNotExists(String bucketName) {
        try {
            boolean found = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );

            if (!found) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build()
                );
            }
        } catch (Exception e) {
            log.error("Ошибка при инициализации MinIO: {}", e.getMessage());
        }
    }
}