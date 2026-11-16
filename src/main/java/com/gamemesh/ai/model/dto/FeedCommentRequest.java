package com.gamemesh.ai.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FeedCommentRequest(
        @NotBlank(message = "gamerTag is required")
        String gamerTag,

        @NotBlank(message = "commentText is required")
        @Size(max = 500, message = "comment must be at most 500 characters")
        String commentText
) {}

