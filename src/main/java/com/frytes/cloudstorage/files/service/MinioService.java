package com.frytes.cloudstorage.files.service;

import com.frytes.cloudstorage.common.exception.FileUploadException;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

    public void upload(String objectName, InputStream inputStream, String contentType) {
        try {
            log.info("Начинаю загрузку файла. Bucket: {}, Object: {}", userFilesBucket, objectName);
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(userFilesBucket)
                            .object(objectName)
                            .stream(inputStream, -1, 10485760)
                            .contentType(contentType)
                            .build()
            );
            log.info("Файл успешно загружен в MinIO: {}", objectName);
        } catch (Exception e) {
            throw new FileUploadException("Ошибка загрузки файла в MinIO: " + e.getMessage(), e);
        }
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
                log.info("Бакет '{}' успешно создан!", bucketName);
            } else {
                log.info("Бакет '{}' уже существует.", bucketName);
            }
        } catch (Exception e) {
            log.error("Ошибка при инициализации MinIO: {}", e.getMessage());
        }
    }
}