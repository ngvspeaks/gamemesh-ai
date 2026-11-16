package com.gamemesh.ai.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "feed_dislikes", uniqueConstraints = {
        @UniqueConstraint(name = "uk_feed_user_dislike", columnNames = {"feed_id", "gamer_tag"})
}, indexes = {
        @Index(name = "idx_dislike_feed_id", columnList = "feed_id"),
        @Index(name = "idx_dislike_gamer_tag", columnList = "gamer_tag")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedDislikeEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "feed_id", nullable = false)
    private UUID feedId;

    @Column(name = "gamer_tag", nullable = false, length = 64)
    private String gamerTag;

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
    }
}

