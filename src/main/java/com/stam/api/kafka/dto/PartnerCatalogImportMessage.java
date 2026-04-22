package com.stam.api.kafka.dto;

import com.stam.api.dto.GameRequestDTO;
import lombok.Data;
import java.util.List;

@Data
public class PartnerCatalogImportMessage {
    private String partnerName;
    private List<GameRequestDTO> games;
}