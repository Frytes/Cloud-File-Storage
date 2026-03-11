package com.frytes.cloudstorage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan("com.frytes.cloudstorage.config.properties")
public class CloudStorageApplication {
	public static void main(String[] args) {
		SpringApplication.run(CloudStorageApplication.class, args);
	}
}
