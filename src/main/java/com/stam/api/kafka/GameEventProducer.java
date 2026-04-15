package com.stam.api.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stam.api.kafka.dto.GameEventMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${stam.kafka.game-events-topic:game.events}")
    private String topic;

    public void publish(String eventType, UUID gameId, String gameTitle) {
        GameEventMessage message = GameEventMessage.builder()
                .schemaVersion(1)
                .eventId(UUID.randomUUID())
                .producedAt(Instant.now())
                .eventType(eventType)
                .gameId(gameId)
                .gameTitle(gameTitle)
                .build();

        String payload;
        try {
            payload = objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            log.warn("Impossible de sérialiser GameEventMessage: {}", e.getMessage());
            return;
        }

        try {
            kafkaTemplate.send(topic, gameId.toString(), payload);
            log.info("Game event publié (topic={}, type={}, gameId={})", topic, eventType, gameId);
        } catch (Exception e) {
            log.warn("Impossible de publier le game event sur Kafka: {}", e.getMessage());
        }
    }
}
