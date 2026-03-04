package com.frytes.cloudstorage.files.service;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
            log.error("Ошибка при инициализации MinIO: " + e.getMessage());
        }
    }
}