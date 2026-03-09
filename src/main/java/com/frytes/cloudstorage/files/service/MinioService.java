package com.frytes.cloudstorage.files.service;

import com.frytes.cloudstorage.common.exception.DirectoryCreationException;
import com.frytes.cloudstorage.common.exception.FileUploadException;
import com.frytes.cloudstorage.common.exception.StorageOperationException;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.*;
import jakarta.annotation.PostConstruct;
// import lombok.RequiredArgsConstructor; // Убираем, так как нужен конструктор с @Qualifier
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class MinioService {

    private final MinioClient minioClient;
    private final MinioClient signerMinioClient;

    @Value("${minio.buckets.user-files}")
    private String userFilesBucket;

    @Value("${minio.buckets.temp-archives}")
    private String tempArchivesBucket;

    public MinioService(MinioClient minioClient,
                        @Qualifier("signerMinioClient") MinioClient signerMinioClient) {
        this.minioClient = minioClient;
        this.signerMinioClient = signerMinioClient;
    }

    @PostConstruct
    public void init() {
        createBucketIfNotExists(userFilesBucket);
        createBucketIfNotExists(tempArchivesBucket);
        configureLifecyclePolicy(tempArchivesBucket);
    }

    public String getPresignedUrl(String objectName) {
        try {
            return signerMinioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(tempArchivesBucket)
                            .object(objectName)
                            .expiry(1, TimeUnit.HOURS)
                            .build()
            );
        } catch (Exception e) {
            throw new StorageOperationException("Ошибка генерации ссылки: " + e.getMessage(), e);
        }
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

    public void uploadArchive(String objectName, InputStream inputStream) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(tempArchivesBucket)
                            .object(objectName)
                            .stream(inputStream, -1, 10485760)
                            .contentType("application/zip")
                            .build()
            );
        } catch (Exception e) {
            throw new FileUploadException("Ошибка сохранения архива в temp-archives: " + e.getMessage(), e);
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

    public Iterable<Result<Item>> listObjectsRecursive(String prefix) {
        return minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(userFilesBucket)
                        .prefix(prefix)
                        .recursive(true)
                        .build()
        );
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

    public StatObjectResponse getMetadata(String objectName) {
        try {
            return minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(userFilesBucket)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            throw new StorageOperationException("Ошибка получения метаданных файла: " + e.getMessage(), e);
        }
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