package com.gamemesh.ai.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateProfileRequest(
        @NotBlank(message = "gamerTag is required")
        @Size(min = 3, max = 32, message = "gamerTag must be between 3 and 32 characters")
        String gamerTag,

        @NotBlank(message = "displayName is required")
        String displayName,

        String bio,
        String avatarUrl,
        String rankTier,
        String favoriteGame
) {}

