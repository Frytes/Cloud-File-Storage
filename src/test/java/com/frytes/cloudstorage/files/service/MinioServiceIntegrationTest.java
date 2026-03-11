package com.frytes.cloudstorage.files.service;

import com.frytes.cloudstorage.TestcontainersConfiguration;
import com.frytes.cloudstorage.config.properties.MinioProperties;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class MinioServiceIntegrationTest {

    @Autowired
    private MinioService minioService;

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioProperties minioProperties;

    @Test
    void shouldCreateDirectoryInMinio() {
        String folderName = "user-1-files/test-folder/";

        minioService.createDirectory(folderName);

        assertThatCode(() -> minioClient.statObject(
                StatObjectArgs.builder()
                        .bucket(minioProperties.buckets().userFiles())
                        .object(folderName)
                        .build()
        )).doesNotThrowAnyException();
    }
}