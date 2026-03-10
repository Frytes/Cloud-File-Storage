package com.frytes.cloudstorage;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

	@Bean
	@ServiceConnection
	PostgreSQLContainer<?> postgresContainer() {
		return new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"));
	}
	@Bean
	@ServiceConnection
	RabbitMQContainer rabbitContainer() {
		return new RabbitMQContainer(DockerImageName.parse("rabbitmq:3-management"));
	}

	@Bean
	@SuppressWarnings("resource")
	GenericContainer<?> minioContainer() {
		return new GenericContainer<>(DockerImageName.parse("minio/minio:latest"))
				.withExposedPorts(9000)
				.withEnv("MINIO_ROOT_USER", "minioadmin")
				.withEnv("MINIO_ROOT_PASSWORD", "minioadmin")
				.withCommand("server /data");
	}

	@Bean
	DynamicPropertyRegistrar minioProperties(GenericContainer<?> minioContainer) {
		return registry -> {
			registry.add("minio.url", () -> "http://" + minioContainer.getHost() + ":" + minioContainer.getMappedPort(9000));
			registry.add("minio.external-url", () -> "http://" + minioContainer.getHost() + ":" + minioContainer.getMappedPort(9000));
		};
	}

	@Bean
	@SuppressWarnings("resource")
	GenericContainer<?> redisContainer() {
		return new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
				.withExposedPorts(6379);
	}

	@Bean
	DynamicPropertyRegistrar redisProperties(GenericContainer<?> redisContainer) {
		return registry -> {
			registry.add("spring.data.redis.host", redisContainer::getHost);
			registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379));
		};
	}
}
