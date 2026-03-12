package com.frytes.cloudstorage.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "minio")
public record MinioProperties(
        String url,
        String externalUrl,
        String accessKey,
        String secretKey,
        Buckets buckets,
        long streamPartSize
) {
    public record Buckets(String userFiles, String tempArchives) {}
}