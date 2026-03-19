package com.stam.api.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stam.api.dto.GameRequestDTO;
import com.stam.api.service.GameService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Consomme le topic d'import catalogue et insère les jeux en base en arrière-plan.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CatalogImportConsumer {

    private final GameService gameService;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @KafkaListener(
            topics = "${stam.kafka.catalog-import-topic:stam.catalog.import}",
            groupId = "${spring.kafka.consumer.group-id:stam-group}"
    )
    public void onMessage(String payload) {
        try {
            GameRequestDTO dto = objectMapper.readValue(payload, GameRequestDTO.class);
            gameService.createGame(dto);
        } catch (JsonProcessingException e) {
            log.warn("Message Kafka invalide: {}", payload);
        } catch (ResponseStatusException e) {
            log.warn("Message Kafka rejeté: {}", e.getReason());
        }
    }
}
