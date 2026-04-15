package com.stam.api.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stam.api.kafka.dto.GameEventMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameEventConsumer {

    private final ObjectMapper objectMapper;
    private final ConcurrentLinkedDeque<GameEventMessage> recentEvents = new ConcurrentLinkedDeque<>();
    private static final int MAX_EVENTS = 50;

    @KafkaListener(
            topics = "${stam.kafka.game-events-topic:game.events}",
            groupId = "${spring.kafka.consumer.group-id:stam-group}"
    )
    public void onMessage(String payload) {
        GameEventMessage message;
        try {
            message = objectMapper.readValue(payload, GameEventMessage.class);
        } catch (JsonProcessingException e) {
            log.warn("Message Kafka game.events invalide (JSON): {}", e.getMessage());
            return;
        }

        log.info("Game event reçu (type={}, gameId={}, title={})",
                message.getEventType(), message.getGameId(), message.getGameTitle());

        recentEvents.addFirst(message);
        while (recentEvents.size() > MAX_EVENTS) {
            recentEvents.removeLast();
        }
    }

    public List<GameEventMessage> getRecentEvents() {
        return List.copyOf(recentEvents);
    }
}
