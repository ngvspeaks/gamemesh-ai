package com.gamemesh.ai.model.entity;

import com.gamemesh.ai.model.enums.SentimentScore;
import com.gamemesh.ai.model.enums.SeverityLevel;
import com.gamemesh.ai.model.enums.TelemetryCategory;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "community_feed", indexes = {
        @Index(name = "idx_feed_created_at", columnList = "created_at"),
        @Index(name = "idx_feed_severity", columnList = "severity"),
        @Index(name = "idx_feed_game_id", columnList = "game_id"),
        @Index(name = "idx_feed_telemetry_id", columnList = "telemetry_event_id"),
        @Index(name = "idx_feed_author", columnList = "author_gamer_tag")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityFeedEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "telemetry_event_id")
    private UUID telemetryEventId;

    @Column(name = "game_id", nullable = false, length = 64)
    private String gameId;

    @Column(name = "author_gamer_tag", length = 64)
    private String authorGamerTag;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 32)
    private TelemetryCategory category;

    @Column(name = "headline", nullable = false, length = 255)
    private String headline;

    @Column(name = "summary", columnDefinition = "TEXT", nullable = false)
    private String summary;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 32)
    private SeverityLevel severity;

    @Enumerated(EnumType.STRING)
    @Column(name = "sentiment", nullable = false, length = 32)
    private SentimentScore sentiment;

    @Column(name = "performance_impact_score", nullable = false)
    private Double performanceImpactScore;

    @Column(name = "recommended_action", length = 255)
    private String recommendedAction;

    @Column(name = "like_count", nullable = false)
    @Builder.Default
    private int likeCount = 0;

    @Column(name = "dislike_count", nullable = false)
    @Builder.Default
    private int dislikeCount = 0;

    @Column(name = "comment_count", nullable = false)
    @Builder.Default
    private int commentCount = 0;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        if (this.performanceImpactScore == null) {
            this.performanceImpactScore = 0.0;
        }
    }
}
