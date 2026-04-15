package com.stam.api.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stam.api.dto.GameRequestDTO;
import com.stam.api.kafka.dto.PartnerCatalogImportMessage;
import com.stam.api.service.GameServicePort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Consomme le topic d'import catalogue et insère les jeux en base en arrière-plan.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CatalogImportConsumer {

    private final GameServicePort gameService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @KafkaListener(
            topics = "${stam.kafka.catalog-import-topic:partner.catalog.import}",
            groupId = "${spring.kafka.consumer.group-id:stam-group}"
    )
    public void onMessage(String payload) {
        PartnerCatalogImportMessage message;
        try {
            message = objectMapper.readValue(payload, PartnerCatalogImportMessage.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Message Kafka invalide (JSON)", e);
        }

        Set<ConstraintViolation<PartnerCatalogImportMessage>> violations = validator.validate(message);
        if (!violations.isEmpty()) {
            String first = violations.iterator().next().getPropertyPath() + ": " + violations.iterator().next().getMessage();
            throw new RuntimeException("Message Kafka rejeté (validation): " + first);
        }

        GameRequestDTO dto = message.getGame();
        log.info("Import catalogue (partnerId={}, mode={}, eventId={})", message.getPartnerId(), message.getMode(), message.getEventId());
        gameService.createGame(dto);
    }
}
