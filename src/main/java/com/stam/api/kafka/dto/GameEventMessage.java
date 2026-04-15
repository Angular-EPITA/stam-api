package com.stam.api.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameEventMessage {

    private int schemaVersion;
    private UUID eventId;
    private Instant producedAt;
    private String eventType;   // CREATED, UPDATED, DELETED
    private UUID gameId;
    private String gameTitle;
}
