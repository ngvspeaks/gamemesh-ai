package com.gamemesh.ai.model.entity;

import com.gamemesh.ai.model.enums.TelemetryCategory;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "telemetry_events", indexes = {
        @Index(name = "idx_telemetry_game_id", columnList = "game_id"),
        @Index(name = "idx_telemetry_player_id", columnList = "player_id"),
        @Index(name = "idx_telemetry_received_at", columnList = "received_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelemetryEventEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "game_id", nullable = false, length = 64)
    private String gameId;

    @Column(name = "player_id", nullable = false, length = 64)
    private String playerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 32)
    private TelemetryCategory category;

    @Column(name = "event_type", nullable = false, length = 64)
    private String eventType;

    @Column(name = "raw_payload", columnDefinition = "TEXT", nullable = false)
    private String rawPayload;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @PrePersist
    public void prePersist() {
        if (this.receivedAt == null) {
            this.receivedAt = Instant.now();
        }
    }
}

