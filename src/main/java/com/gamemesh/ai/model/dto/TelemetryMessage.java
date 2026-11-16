package com.gamemesh.ai.model.dto;

import com.gamemesh.ai.model.enums.TelemetryCategory;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * AMQP message contract exchanged between IngestionService and Consumer.
 */
public record TelemetryMessage(
        UUID eventId,
        String gameId,
        String playerId,
        TelemetryCategory category,
        String eventType,
        String payload,
        Instant timestamp
) implements Serializable {}

