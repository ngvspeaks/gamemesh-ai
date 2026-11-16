package com.gamemesh.ai.model.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Acknowledgment response returned immediately after enqueuing telemetry to RabbitMQ.
 */
public record TelemetryIngestResponse(
        UUID eventId,
        String status,
        String message,
        Instant enqueuedAt
) {
    public static TelemetryIngestResponse accepted(UUID eventId) {
        return new TelemetryIngestResponse(
                eventId,
                "ACCEPTED",
                "Telemetry event accepted and queued for autonomous curation",
                Instant.now()
        );
    }
}

