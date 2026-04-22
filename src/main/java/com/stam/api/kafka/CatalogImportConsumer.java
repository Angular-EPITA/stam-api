package com.stam.api.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stam.api.dto.GameRequestDTO;
import com.stam.api.kafka.dto.PartnerCatalogImportMessage;
import com.stam.api.service.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class CatalogImportConsumer {

    private static final Logger log = LoggerFactory.getLogger(CatalogImportConsumer.class);
    private final GameService gameService;
    private final ObjectMapper objectMapper;

    public CatalogImportConsumer(GameService gameService, ObjectMapper objectMapper) {
        this.gameService = gameService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${stam.kafka.catalog-import-topic:partner.catalog.import}", groupId = "stam-group")
    public void consume(String payload) {
        try {
            // Transformation du JSON (String) vers l'objet Java
            PartnerCatalogImportMessage message = objectMapper.readValue(payload, PartnerCatalogImportMessage.class);
            
            log.info("Début du traitement du catalogue de : {}", message.getPartnerName());
            
            int successCount = 0;
            for (GameRequestDTO gameDto : message.getGames()) {
                try {
                    gameService.createGame(gameDto);
                    successCount++;
                } catch (Exception e) {
                    log.error("Erreur lors de l'import du jeu '{}' : {}", gameDto.getTitle(), e.getMessage());
                }
            }
            log.info("Importation terminée pour {}. {}/{} jeux insérés.", message.getPartnerName(), successCount, message.getGames().size());

        } catch (JsonProcessingException e) {
            log.error("Erreur : impossible de lire le message JSON reçu : {}", e.getMessage());
        }
    }
}
