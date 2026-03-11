package com.frytes.cloudstorage.config;

import com.frytes.cloudstorage.config.properties.MinioProperties;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@RequiredArgsConstructor
public class MinioConfig {

    private final MinioProperties minioProperties;

    @Bean
    @Primary
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(minioProperties.url())
                .credentials(minioProperties.accessKey(), minioProperties.secretKey())
                .build();
    }

    @Bean(name = "signerMinioClient")
    public MinioClient signerMinioClient() {
        String extUrl = minioProperties.externalUrl();
        String endpoint = (extUrl != null && !extUrl.isBlank()) ? extUrl : minioProperties.url();
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(minioProperties.accessKey(), minioProperties.secretKey())
                .build();
    }
}