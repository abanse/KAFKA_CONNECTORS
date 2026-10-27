package com.yms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * REST Template Konfiguration für die Kommunikation mit externen APIs.
 */
@Configuration
public class RestTemplateConfig {

    @Value("${yms.api.connection-timeout-ms:5000}")
    private int connectionTimeout;

    @Value("${yms.api.read-timeout-ms:10000}")
    private int readTimeout;

    /**
     * Konfiguriert RestTemplate mit Timeouts und Error Handlers
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofMillis(connectionTimeout))
                .setReadTimeout(Duration.ofMillis(readTimeout))
                .requestFactory(this::clientHttpRequestFactory)
                .build();
    }

    /**
     * Erstellt eine SimpleClientHttpRequestFactory mit konfigurierten Timeouts
     */
    private SimpleClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectionTimeout);
        factory.setReadTimeout(readTimeout);
        factory.setBufferRequestBody(true);
        return factory;
    }
}