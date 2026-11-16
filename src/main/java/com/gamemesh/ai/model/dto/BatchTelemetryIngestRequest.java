package com.gamemesh.ai.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * High-frequency batch payload container for ingesting multiple game events in a single HTTP call.
 */
public record BatchTelemetryIngestRequest(
        @NotEmpty(message = "Events list must not be empty")
        List<@Valid TelemetryIngestRequest> events
) {}

