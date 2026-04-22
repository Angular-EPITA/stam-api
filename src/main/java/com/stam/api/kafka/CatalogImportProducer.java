package com.stam.api.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stam.api.kafka.dto.PartnerCatalogImportMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class CatalogImportProducer {

    private static final Logger log = LoggerFactory.getLogger(CatalogImportProducer.class);
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${stam.kafka.catalog-import-topic:partner.catalog.import}")
    private String topicName;

    public CatalogImportProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendCatalogImportMessage(PartnerCatalogImportMessage message) {
        log.info("Envoi d'un message d'importation pour le partenaire : {}", message.getPartnerName());
        
        try {
            // Transformation de l'objet en JSON (String)
            String payload = objectMapper.writeValueAsString(message);
            kafkaTemplate.send(topicName, payload);
            log.info("Message envoyé avec succès sur le topic {}", topicName);
        } catch (JsonProcessingException e) {
            log.error("Erreur lors de la sérialisation du catalogue : {}", e.getMessage());
        }
    }
}