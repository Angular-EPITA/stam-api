package com.stam.api.controller;

import com.stam.api.dto.GameRequestDTO;
import com.stam.api.kafka.CatalogImportProducer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/partners")
@RequiredArgsConstructor
@Tag(name = "Partenaires", description = "Endpoints pour l'alimentation du catalogue par partenaires externes")
public class PartnerCatalogController {

    private final CatalogImportProducer catalogImportProducer;

    @Operation(
            summary = "Importer un lot de jeux (Partenaire, Asynchrone)",
            description = "Reçoit une liste de jeux d'un partenaire et les publie sur Kafka pour traitement asynchrone."
    )
    @PostMapping("/{partnerId}/catalog/import-async")
    public ResponseEntity<String> importPartnerCatalogAsync(
            @PathVariable String partnerId,
            @Valid @RequestBody List<@Valid GameRequestDTO> games
    ) {
        int count = catalogImportProducer.publishBatch(partnerId, "HTTP_PARTNER", games);
        return ResponseEntity.accepted().body(count + " jeu(x) envoyé(s) dans Kafka pour import (partnerId=" + partnerId + ").");
    }
}
