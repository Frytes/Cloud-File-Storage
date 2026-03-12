package com.frytes.cloudstorage.files.repository.minio;

import com.frytes.cloudstorage.common.exception.FileUploadException;
import com.frytes.cloudstorage.common.exception.StorageOperationException;
import com.frytes.cloudstorage.config.properties.AppProperties;
import com.frytes.cloudstorage.config.properties.MinioProperties;
import com.frytes.cloudstorage.files.repository.ArchiveStorageRepository;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Slf4j
@Repository
public class MinioArchiveRepository implements ArchiveStorageRepository {

    private final MinioProperties minioProperties;
    private final MinioClient minioClient;
    private final MinioClient signerMinioClient;
    private final String tempArchivesBucket;
    private final int expirationHours;


    public MinioArchiveRepository(
            MinioProperties minioProperties1, MinioClient minioClient,
            @Qualifier("signerMinioClient") MinioClient signerMinioClient,
            MinioProperties minioProperties,
            AppProperties appProperties) {
        this.minioProperties = minioProperties1;
        this.minioClient = minioClient;
        this.signerMinioClient = signerMinioClient;
        this.tempArchivesBucket = minioProperties.buckets().tempArchives();
        this.expirationHours = (int) appProperties.archive().expirationHours();
    }

    @Override
    public void uploadArchive(String objectName, InputStream inputStream) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(tempArchivesBucket)
                            .object(objectName)
                            .stream(inputStream, -1, minioProperties.streamPartSize())
                            .contentType("application/zip")
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to upload archive to MinIO. Object: {}", objectName, e);
            throw new FileUploadException("Ошибка сохранения архива", e);
        }
    }

    @Override
    public String getPresignedUrl(String objectName) {
        try {
            return signerMinioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(tempArchivesBucket)
                            .object(objectName)
                            .expiry(expirationHours, TimeUnit.HOURS)
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to generate presigned URL in MinIO. Object: {}", objectName, e);
            throw new StorageOperationException("Ошибка генерации ссылки на архив", e);
        }
    }
}