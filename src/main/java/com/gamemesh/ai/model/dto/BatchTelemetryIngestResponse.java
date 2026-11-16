package com.gamemesh.ai.model.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * High-frequency batch acknowledgment returned after queueing all events.
 */
public record BatchTelemetryIngestResponse(
        int totalAccepted,
        List<UUID> eventIds,
        String status,
        long elapsedProcessingMs,
        Instant enqueuedAt
) {
    public static BatchTelemetryIngestResponse accepted(List<UUID> eventIds, long elapsedProcessingMs) {
        return new BatchTelemetryIngestResponse(
                eventIds.size(),
                eventIds,
                "ACCEPTED",
                elapsedProcessingMs,
                Instant.now()
        );
    }
}

