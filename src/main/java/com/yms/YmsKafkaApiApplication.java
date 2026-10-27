package com.yms;

import com.yms.config.ValidationConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Hauptklasse der YMS Kafka API Anwendung.
 */
@SpringBootApplication
@EnableConfigurationProperties(ValidationConfig.class)
public class YmsKafkaApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(YmsKafkaApiApplication.class, args);
    }
}
