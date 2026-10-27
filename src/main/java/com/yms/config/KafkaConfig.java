package com.yms.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka Konfiguration - erstellt automatisch die benötigten Topics.
 * Erweitert um LKW-Einweisungs-Topic.
 */
@Configuration
public class KafkaConfig {

    @Value("${kafka.topics.delivery}")
    private String deliveryTopic;

    @Value("${kafka.topics.weighing-in}")
    private String weighingInTopic;

    @Value("${kafka.topics.weighing-out}")
    private String weighingOutTopic;

    @Value("${kafka.topics.wiegedaten}")
    private String wiegedatenTopic;



    @Bean
    public NewTopic deliveryTopic() {
        return TopicBuilder.name(deliveryTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic weighingInTopic() {
        return TopicBuilder.name(weighingInTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic weighingOutTopic() {
        return TopicBuilder.name(weighingOutTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic wiegedatenTopic() {
        return TopicBuilder.name(wiegedatenTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }


}