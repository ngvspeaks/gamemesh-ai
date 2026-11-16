package com.gamemesh.ai.model.dto;

import com.gamemesh.ai.model.enums.TelemetryCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePostRequest(
        @NotBlank(message = "gamerTag is required")
        String gamerTag,

        @NotBlank(message = "gameId is required")
        String gameId,

        TelemetryCategory category,

        @NotBlank(message = "headline is required")
        @Size(max = 200, message = "headline cannot exceed 200 characters")
        String headline,

        @NotBlank(message = "content is required")
        @Size(max = 1000, message = "content cannot exceed 1000 characters")
        String content
) {}

