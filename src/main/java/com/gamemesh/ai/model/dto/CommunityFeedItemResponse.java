package com.gamemesh.ai.model.dto;

import com.gamemesh.ai.model.entity.CommunityFeedEntity;
import com.gamemesh.ai.model.enums.SentimentScore;
import com.gamemesh.ai.model.enums.SeverityLevel;
import com.gamemesh.ai.model.enums.TelemetryCategory;

import java.time.Instant;
import java.util.UUID;

/**
 * Public DTO representing an AI-curated community feed item with social engagement metrics.
 */
public record CommunityFeedItemResponse(
        UUID feedId,
        UUID telemetryEventId,
        String gameId,
        String authorGamerTag,
        TelemetryCategory category,
        String headline,
        String summary,
        SeverityLevel severity,
        SentimentScore sentiment,
        Double performanceImpactScore,
        String recommendedAction,
        int likeCount,
        int dislikeCount,
        int commentCount,
        Instant createdAt
) {
    public static CommunityFeedItemResponse fromEntity(CommunityFeedEntity entity) {
        return new CommunityFeedItemResponse(
                entity.getId(),
                entity.getTelemetryEventId(),
                entity.getGameId(),
                entity.getAuthorGamerTag() != null ? entity.getAuthorGamerTag() : "GameMesh AI",
                entity.getCategory(),
                entity.getHeadline(),
                entity.getSummary(),
                entity.getSeverity(),
                entity.getSentiment(),
                entity.getPerformanceImpactScore(),
                entity.getRecommendedAction(),
                entity.getLikeCount(),
                entity.getDislikeCount(),
                entity.getCommentCount(),
                entity.getCreatedAt()
        );
    }
}
