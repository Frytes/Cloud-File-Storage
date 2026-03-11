package com.frytes.cloudstorage.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.rabbitmq")
public record RabbitMqProperties(
        String username,
        String password,
        Stomp stomp
) {
    public record Stomp(String host, int port) {}
}