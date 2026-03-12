package com.frytes.cloudstorage.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Frontend frontend,
        Search search,
        Upload upload,
        Archive archive
) {
    public record Frontend(String url) {}
    public record Search(int limit) {}
    public record Upload(int maxFilesPerRequest) {}
    public record Archive(
            long expirationHours,
            int timeoutMinutes,
            int bufferSizeBytes,
            long syncZipLimitBytes,
            Executor executor
    ) {
        public record Executor(int corePoolSize, int maxPoolSize, int queueCapacity) {}
    }
}