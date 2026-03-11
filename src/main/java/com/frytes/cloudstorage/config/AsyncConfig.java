package com.frytes.cloudstorage.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AsyncConfig {

    @Value("${app.archive.executor.core-pool-size:2}")
    private int corePoolSize;

    @Value("${app.archive.executor.max-pool-size:5}")
    private int maxPoolSize;

    @Value("${app.archive.executor.queue-capacity:50}")
    private int queueCapacity;

    @Bean(name = "archiveTaskExecutor")
    public TaskExecutor archiveTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("ArchiveWorker-");
        executor.initialize();
        return executor;
    }
}