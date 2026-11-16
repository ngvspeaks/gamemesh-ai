package com.gamemesh.ai.model.dto;

import com.gamemesh.ai.model.entity.FeedCommentEntity;

import java.time.Instant;
import java.util.UUID;

public record FeedCommentResponse(
        UUID commentId,
        UUID feedId,
        String gamerTag,
        String commentText,
        Instant createdAt
) {
    public static FeedCommentResponse fromEntity(FeedCommentEntity entity) {
        return new FeedCommentResponse(
                entity.getId(),
                entity.getFeedId(),
                entity.getGamerTag(),
                entity.getCommentText(),
                entity.getCreatedAt()
        );
    }
}

