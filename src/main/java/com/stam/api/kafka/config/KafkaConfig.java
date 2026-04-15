package com.stam.api.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfig {

    @Value("${stam.kafka.catalog-import-topic:partner.catalog.import}")
    private String catalogImportTopic;

    @Value("${stam.kafka.game-events-topic:game.events}")
    private String gameEventsTopic;

    // --- Topics ---

    @Bean
    NewTopic catalogImportTopic() {
        return TopicBuilder.name(catalogImportTopic).partitions(3).replicas(1).build();
    }

    @Bean
    NewTopic catalogImportDltTopic() {
        return TopicBuilder.name(catalogImportTopic + ".dlt").partitions(1).replicas(1).build();
    }

    @Bean
    NewTopic gameEventsTopic() {
        return TopicBuilder.name(gameEventsTopic).partitions(3).replicas(1).build();
    }

    // --- Error handling (DLT) ---

    @Bean
    DeadLetterPublishingRecoverer dltRecoverer(KafkaTemplate<String, String> kafkaTemplate) {
        return new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, ex) -> new TopicPartition(record.topic() + ".dlt", record.partition()));
    }

    @Bean
    DefaultErrorHandler kafkaErrorHandler(DeadLetterPublishingRecoverer dltRecoverer) {
        return new DefaultErrorHandler(dltRecoverer, new FixedBackOff(1000L, 3L));
    }
}
