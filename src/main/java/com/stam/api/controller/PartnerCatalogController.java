package com.stam.api.controller;

import com.stam.api.kafka.CatalogImportProducer;
import com.stam.api.kafka.dto.PartnerCatalogImportMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/partners")
@Tag(name = "Partenaires", description = "Endpoints d'ingestion asynchrone via Kafka")
public class PartnerCatalogController {

    private final CatalogImportProducer catalogImportProducer;

    public PartnerCatalogController(CatalogImportProducer catalogImportProducer) {
        this.catalogImportProducer = catalogImportProducer;
    }

    @Operation(summary = "Importer un catalogue partenaire", description = "Envoie le catalogue dans Kafka pour un traitement asynchrone.")
    @PostMapping("/catalog")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> importCatalog(@RequestBody PartnerCatalogImportMessage message) {
        catalogImportProducer.sendCatalogImportMessage(message);
        return ResponseEntity.accepted().body("Le catalogue du partenaire " + message.getPartnerName() + " est en cours de traitement asynchrone.");
    }
}
