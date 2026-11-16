package com.gamemesh.ai.controller;

import com.gamemesh.ai.model.dto.*;
import com.gamemesh.ai.model.enums.SeverityLevel;
import com.gamemesh.ai.service.AgentService;
import com.gamemesh.ai.service.SocialInteractionService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/feed")
public class FeedController {

    private final AgentService agentService;
    private final SocialInteractionService socialService;

    public FeedController(AgentService agentService, SocialInteractionService socialService) {
        this.agentService = agentService;
        this.socialService = socialService;
    }

    /**
     * Retrieve the live, AI-curated community feed.
     * Supports filtering by severity (LOW, MEDIUM, HIGH, CRITICAL) and custom limit.
     */
    @GetMapping
    public ResponseEntity<List<CommunityFeedItemResponse>> getLiveCommunityFeed(
            @RequestParam(required = false) SeverityLevel severity,
            @RequestParam(defaultValue = "50") int limit
    ) {
        List<CommunityFeedItemResponse> feed = agentService.getLiveFeed(severity, limit)
                .stream()
                .map(CommunityFeedItemResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(feed);
    }

    /**
     * Retrieve a specific feed item by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CommunityFeedItemResponse> getFeedItemById(@PathVariable UUID id) {
        return agentService.getFeedItem(id)
                .map(CommunityFeedItemResponse::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Publish a new social post directly to the arena feed.
     */
    @PostMapping("/posts")
    public ResponseEntity<CommunityFeedItemResponse> createPost(@Valid @RequestBody CreatePostRequest request) {
        CommunityFeedItemResponse response = socialService.createManualPost(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Like a feed item for a specific gamer (toggles on/off).
     */
    @PostMapping("/{id}/like")
    public ResponseEntity<FeedReactionResponse> toggleFeedLike(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "vortex_99") String gamerTag
    ) {
        FeedReactionResponse response = socialService.toggleLike(id, gamerTag);
        return ResponseEntity.ok(response);
    }

    /**
     * Dislike a feed item for a specific gamer (toggles on/off).
     */
    @PostMapping("/{id}/dislike")
    public ResponseEntity<FeedReactionResponse> toggleFeedDislike(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "vortex_99") String gamerTag
    ) {
        FeedReactionResponse response = socialService.toggleDislike(id, gamerTag);
        return ResponseEntity.ok(response);
    }

    /**
     * Post a comment to a feed item.
     */
    @PostMapping("/{id}/comments")
    public ResponseEntity<FeedCommentResponse> addComment(
            @PathVariable UUID id,
            @Valid @RequestBody FeedCommentRequest request
    ) {
        FeedCommentResponse response = socialService.addComment(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieve comments for a feed item.
     */
    @GetMapping("/{id}/comments")
    public ResponseEntity<List<FeedCommentResponse>> getComments(@PathVariable UUID id) {
        List<FeedCommentResponse> comments = socialService.getComments(id);
        return ResponseEntity.ok(comments);
    }
}
