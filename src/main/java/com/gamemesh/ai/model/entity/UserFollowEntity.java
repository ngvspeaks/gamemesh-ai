package com.gamemesh.ai.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_follows", uniqueConstraints = {
        @UniqueConstraint(name = "uk_follow_pair", columnNames = { "follower_tag", "following_tag" })
}, indexes = {
        @Index(name = "idx_follow_follower", columnList = "follower_tag"),
        @Index(name = "idx_follow_following", columnList = "following_tag")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFollowEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "follower_tag", nullable = false, length = 64)
    private String followerTag;

    @Column(name = "following_tag", nullable = false, length = 64)
    private String followingTag;

    @Column(name = "followed_at", nullable = false)
    private Instant followedAt;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.followedAt == null) {
            this.followedAt = Instant.now();
        }
    }
}
