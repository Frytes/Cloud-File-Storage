package com.frytes.cloudstorage.config;

import com.frytes.cloudstorage.config.properties.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@RequiredArgsConstructor
public class AsyncConfig {
    private final AppProperties appProperties;

    @Bean(name = "archiveTaskExecutor")
    public TaskExecutor archiveTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(appProperties.archive().executor().corePoolSize());
        executor.setMaxPoolSize(appProperties.archive().executor().maxPoolSize());
        executor.setQueueCapacity(appProperties.archive().executor().queueCapacity());
        executor.setThreadNamePrefix("ArchiveWorker-");
        executor.initialize();
        return executor;
    }
}