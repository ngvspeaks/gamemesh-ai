package com.gamemesh.ai.model.dto;

import java.util.UUID;

public record FeedReactionResponse(
        UUID feedId,
        String gamerTag,
        boolean liked,
        boolean disliked,
        int totalLikes,
        int totalDislikes
) {}

