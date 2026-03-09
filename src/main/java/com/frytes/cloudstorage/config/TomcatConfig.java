package com.frytes.cloudstorage.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TomcatConfig {

    @Value("${app.upload.max-files-per-request:10000}")
    private int maxFiles;

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatCustomizer() {
        return factory -> factory.addConnectorCustomizers(connector -> {

            connector.setMaxParameterCount(maxFiles);
            connector.setProperty("maxPartCount", String.valueOf(maxFiles));

        });
    }
}