package com.frytes.cloudstorage.files.repository.minio;

import com.frytes.cloudstorage.config.properties.MinioProperties;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketLifecycleArgs;
import io.minio.messages.Expiration;
import io.minio.messages.LifecycleConfiguration;
import io.minio.messages.LifecycleRule;
import io.minio.messages.RuleFilter;
import io.minio.messages.Status;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MinioInitializer {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    @PostConstruct
    public void init() {
        createBucketIfNotExists(minioProperties.buckets().userFiles());
        createBucketIfNotExists(minioProperties.buckets().tempArchives());
        configureLifecyclePolicy(minioProperties.buckets().tempArchives());
    }

    private void configureLifecyclePolicy(String bucketName) {
        try {
            LifecycleRule rule = new LifecycleRule(
                    Status.ENABLED,
                    null,
                    new Expiration((java.time.ZonedDateTime) null, 1, null),
                    new RuleFilter(""),
                    "expire-old-archives",
                    null,
                    null,
                    null
            );
            LifecycleConfiguration lifecycleConfig = new LifecycleConfiguration(List.of(rule));
            minioClient.setBucketLifecycle(
                    SetBucketLifecycleArgs.builder()
                            .bucket(bucketName)
                            .config(lifecycleConfig)
                            .build()
            );
            log.info("Lifecycle policy настроена для бакета: {} (удаление через 1 день)", bucketName);
        } catch (Exception e) {
            log.error("Не удалось настроить Lifecycle policy для {}: {}", bucketName, e.getMessage());
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
                log.info("Бакет успешно создан: {}", bucketName);
            } else {
                log.info("Бакет уже существует: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("Критическая ошибка при инициализации MinIO бакета {}: {}", bucketName, e.getMessage());
        }
    }
}