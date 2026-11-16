package com.gamemesh.ai;

import com.gamemesh.ai.controller.FeedController;
import com.gamemesh.ai.model.dto.*;
import com.gamemesh.ai.model.entity.CommunityFeedEntity;
import com.gamemesh.ai.model.enums.SentimentScore;
import com.gamemesh.ai.model.enums.SeverityLevel;
import com.gamemesh.ai.model.enums.TelemetryCategory;
import com.gamemesh.ai.service.AgentService;
import com.gamemesh.ai.service.SocialInteractionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedControllerTest {

    @Mock
    private AgentService agentService;

    @Mock
    private SocialInteractionService socialService;

    @InjectMocks
    private FeedController feedController;

    @Test
    void shouldReturnLiveFeedItems() {
        UUID feedId = UUID.randomUUID();
        CommunityFeedEntity entity = CommunityFeedEntity.builder()
                .id(feedId)
                .telemetryEventId(UUID.randomUUID())
                .gameId("match-101")
                .authorGamerTag("vortex_99")
                .category(TelemetryCategory.SYSTEM)
                .headline("Performance Alert")
                .summary("FPS drop detected")
                .severity(SeverityLevel.HIGH)
                .sentiment(SentimentScore.NEGATIVE)
                .performanceImpactScore(0.8)
                .recommendedAction("Check frame pacing")
                .likeCount(10)
                .dislikeCount(2)
                .commentCount(4)
                .createdAt(Instant.now())
                .build();

        when(agentService.getLiveFeed(null, 50)).thenReturn(List.of(entity));

        var response = feedController.getLiveCommunityFeed(null, 50);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Performance Alert", response.getBody().get(0).headline());
        assertEquals(10, response.getBody().get(0).likeCount());
        assertEquals(2, response.getBody().get(0).dislikeCount());
    }

    @Test
    void shouldToggleFeedLike() {
        UUID feedId = UUID.randomUUID();
        FeedReactionResponse reactionResponse = new FeedReactionResponse(feedId, "vortex_99", true, false, 5, 0);

        when(socialService.toggleLike(feedId, "vortex_99")).thenReturn(reactionResponse);

        ResponseEntity<FeedReactionResponse> response = feedController.toggleFeedLike(feedId, "vortex_99");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().liked());
        assertFalse(response.getBody().disliked());
        assertEquals(5, response.getBody().totalLikes());
    }

    @Test
    void shouldToggleFeedDislike() {
        UUID feedId = UUID.randomUUID();
        FeedReactionResponse reactionResponse = new FeedReactionResponse(feedId, "vortex_99", false, true, 4, 1);

        when(socialService.toggleDislike(feedId, "vortex_99")).thenReturn(reactionResponse);

        ResponseEntity<FeedReactionResponse> response = feedController.toggleFeedDislike(feedId, "vortex_99");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().liked());
        assertTrue(response.getBody().disliked());
        assertEquals(1, response.getBody().totalDislikes());
    }

    @Test
    void shouldAddCommentToFeed() {
        UUID feedId = UUID.randomUUID();
        FeedCommentRequest request = new FeedCommentRequest("vortex_99", "Insane clip!");
        FeedCommentResponse commentResponse = new FeedCommentResponse(UUID.randomUUID(), feedId, "vortex_99", "Insane clip!", Instant.now());

        when(socialService.addComment(eq(feedId), any(FeedCommentRequest.class))).thenReturn(commentResponse);

        ResponseEntity<FeedCommentResponse> response = feedController.addComment(feedId, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Insane clip!", response.getBody().commentText());
    }

    @Test
    void shouldCreateManualPost() {
        CreatePostRequest request = new CreatePostRequest("vortex_99", "Apex Legends", "Triple Kill in Ranked", "Wiped squad at bunker", "COMBAT");
        CommunityFeedItemResponse itemResponse = new CommunityFeedItemResponse(
                UUID.randomUUID(), null, "Apex Legends", "vortex_99", "COMBAT",
                "Triple Kill in Ranked", "Wiped squad at bunker", "LOW", "POSITIVE", 0.0,
                "Shared with community feed", 0, 0, 0, Instant.now()
        );

        when(socialService.createManualPost(any(CreatePostRequest.class))).thenReturn(itemResponse);

        ResponseEntity<CommunityFeedItemResponse> response = feedController.createPost(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Triple Kill in Ranked", response.getBody().headline());
    }
}
