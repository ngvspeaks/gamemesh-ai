package com.gamemesh.ai.model.dto;

import com.gamemesh.ai.model.enums.SentimentScore;
import java.time.Instant;
import java.util.UUID;

/**
 * Representation of an AI-curated telemetry assessment.
 */
public record CuratedAnalysisResponse(
        UUID analysisId,
        UUID telemetryEventId,
        SentimentScore sentiment,
        Double toxicityScore,
        String summary,
        boolean flaggedToxicity,
        Instant analyzedAt
) {}

