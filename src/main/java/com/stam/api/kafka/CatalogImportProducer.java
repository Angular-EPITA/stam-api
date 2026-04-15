package com.stam.api.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stam.api.dto.GameRequestDTO;
import com.stam.api.kafka.dto.PartnerCatalogImportMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Publie des messages d'import catalogue sur Kafka.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CatalogImportProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${stam.kafka.catalog-import-topic:partner.catalog.import}")
    private String topic;

    public int publishBatch(String partnerId, String mode, List<GameRequestDTO> games) {
        if (games == null || games.isEmpty()) {
            return 0;
        }
        games.forEach(dto -> publishOne(partnerId, mode, dto));
        return games.size();
    }

    private void publishOne(String partnerId, String mode, GameRequestDTO dto) {
        PartnerCatalogImportMessage message = new PartnerCatalogImportMessage();
        message.setSchemaVersion(1);
        message.setEventId(UUID.randomUUID());
        message.setProducedAt(Instant.now());
        message.setPartnerId(partnerId);
        message.setMode(mode);
        message.setGame(dto);

        String payload;
        try {
            payload = objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            log.warn("Impossible de sérialiser le GameRequestDTO pour Kafka: {}", e.getMessage());
            return;
        }

        kafkaTemplate.send(topic, payload);
        log.info("Message publié (topic={})", topic);
    }
}
