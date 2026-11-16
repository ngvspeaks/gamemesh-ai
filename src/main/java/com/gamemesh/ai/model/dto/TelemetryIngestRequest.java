package com.gamemesh.ai.model.dto;

import com.gamemesh.ai.model.enums.TelemetryCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

/**
 * Inbound telemetry ingest payload.
 */
public record TelemetryIngestRequest(
        @NotBlank(message = "gameId must not be blank")
        String gameId,

        @NotBlank(message = "playerId must not be blank")
        String playerId,

        @NotNull(message = "category is required")
        TelemetryCategory category,

        @NotBlank(message = "eventType must not be blank")
        String eventType,

        @NotBlank(message = "payload content must not be blank")
        String payload,

        Instant timestamp
) {
    public TelemetryIngestRequest {
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }
}

