package com.gamemesh.ai.model.dto;

import java.util.UUID;

public record FeedLikeResponse(
                UUID feedId,
                String gamerTag,
                boolean liked,
                int totalLikes) {
}
