package com.gamemesh.ai.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_profiles", indexes = {
        @Index(name = "idx_profile_gamertag", columnList = "gamer_tag", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "gamer_tag", nullable = false, unique = true, length = 64)
    private String gamerTag;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "avatar_url", length = 512)
    private String avatarUrl;

    @Column(name = "bio", length = 512)
    private String bio;

    @Column(name = "rank_tier", length = 64)
    private String rankTier;

    @Column(name = "reputation_score", nullable = false)
    @Builder.Default
    private int reputationScore = 100;

    @Column(name = "followers_count", nullable = false)
    @Builder.Default
    private int followersCount = 0;

    @Column(name = "following_count", nullable = false)
    @Builder.Default
    private int followingCount = 0;

    @Column(name = "favorite_game", length = 64)
    private String favoriteGame;

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
        if (this.avatarUrl == null || this.avatarUrl.isBlank()) {
            this.avatarUrl = "https://api.dicebear.com/7.x/bottts/svg?seed=" + this.gamerTag;
        }
    }
}

