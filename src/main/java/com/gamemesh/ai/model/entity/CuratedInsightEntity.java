package com.gamemesh.ai.model.entity;

import com.gamemesh.ai.model.enums.SentimentScore;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "curated_insights", indexes = {
        @Index(name = "idx_insight_event_id", columnList = "telemetry_event_id"),
        @Index(name = "idx_insight_sentiment", columnList = "sentiment"),
        @Index(name = "idx_insight_flagged", columnList = "flagged_toxicity")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuratedInsightEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "telemetry_event_id", nullable = false)
    private UUID telemetryEventId;

    @Enumerated(EnumType.STRING)
    @Column(name = "sentiment", nullable = false, length = 32)
    private SentimentScore sentiment;

    @Column(name = "toxicity_score", nullable = false)
    private Double toxicityScore;

    @Column(name = "summary", columnDefinition = "TEXT", nullable = false)
    private String summary;

    @Column(name = "flagged_toxicity", nullable = false)
    private boolean flaggedToxicity;

    @Column(name = "curated_at", nullable = false)
    private Instant curatedAt;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.curatedAt == null) {
            this.curatedAt = Instant.now();
        }
    }
}

