package com.gamemesh.ai.model.dto;

import com.gamemesh.ai.model.entity.UserProfileEntity;

import java.time.Instant;
import java.util.UUID;

public record UserProfileResponse(
        UUID id,
        String gamerTag,
        String displayName,
        String avatarUrl,
        String bio,
        String rankTier,
        int reputationScore,
        int followersCount,
        int followingCount,
        String favoriteGame,
        Instant createdAt
) {
    public static UserProfileResponse fromEntity(UserProfileEntity entity) {
        return new UserProfileResponse(
                entity.getId(),
                entity.getGamerTag(),
                entity.getDisplayName(),
                entity.getAvatarUrl(),
                entity.getBio(),
                entity.getRankTier(),
                entity.getReputationScore(),
                entity.getFollowersCount(),
                entity.getFollowingCount(),
                entity.getFavoriteGame(),
                entity.getCreatedAt()
        );
    }
}

