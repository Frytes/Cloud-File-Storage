package com.frytes.cloudstorage.config;


import com.frytes.cloudstorage.config.properties.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class TomcatConfig {

    private final AppProperties appProperties;

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatCustomizer() {
        return factory -> factory.addConnectorCustomizers(connector -> {
            connector.setMaxParameterCount(appProperties.upload().maxFilesPerRequest());
            connector.setProperty("maxPartCount", String.valueOf(appProperties.upload().maxFilesPerRequest()));

        });
    }
}