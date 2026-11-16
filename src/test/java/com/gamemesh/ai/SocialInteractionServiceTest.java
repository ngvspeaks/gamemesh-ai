package com.gamemesh.ai;

import com.gamemesh.ai.model.dto.*;
import com.gamemesh.ai.model.entity.*;
import com.gamemesh.ai.repository.*;
import com.gamemesh.ai.service.SocialInteractionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SocialInteractionServiceTest {

    @Mock
    private CommunityFeedRepository communityFeedRepository;

    @Mock
    private FeedLikeRepository feedLikeRepository;

    @Mock
    private FeedDislikeRepository feedDislikeRepository;

    @Mock
    private FeedCommentRepository feedCommentRepository;

    private SocialInteractionService socialService;

    @BeforeEach
    void setUp() {
        socialService = new SocialInteractionService(
                communityFeedRepository,
                feedLikeRepository,
                feedDislikeRepository,
                feedCommentRepository
        );
    }

    @Test
    void shouldToggleLikeOnFeedItem() {
        UUID feedId = UUID.randomUUID();
        CommunityFeedEntity feed = CommunityFeedEntity.builder()
                .id(feedId)
                .likeCount(0)
                .dislikeCount(0)
                .build();

        when(communityFeedRepository.findById(feedId)).thenReturn(Optional.of(feed));
        when(feedLikeRepository.findByFeedIdAndGamerTagIgnoreCase(feedId, "vortex_99")).thenReturn(Optional.empty());
        when(feedDislikeRepository.findByFeedIdAndGamerTagIgnoreCase(feedId, "vortex_99")).thenReturn(Optional.empty());

        FeedReactionResponse response = socialService.toggleLike(feedId, "vortex_99");

        assertTrue(response.liked());
        assertFalse(response.disliked());
        assertEquals(1, response.totalLikes());
        assertEquals(1, feed.getLikeCount());
        verify(feedLikeRepository, times(1)).save(any(FeedLikeEntity.class));
    }

    @Test
    void shouldToggleUnlikeWhenAlreadyLiked() {
        UUID feedId = UUID.randomUUID();
        CommunityFeedEntity feed = CommunityFeedEntity.builder()
                .id(feedId)
                .likeCount(1)
                .dislikeCount(0)
                .build();

        FeedLikeEntity existingLike = FeedLikeEntity.builder()
                .id(UUID.randomUUID())
                .feedId(feedId)
                .gamerTag("vortex_99")
                .createdAt(Instant.now())
                .build();

        when(communityFeedRepository.findById(feedId)).thenReturn(Optional.of(feed));
        when(feedLikeRepository.findByFeedIdAndGamerTagIgnoreCase(feedId, "vortex_99")).thenReturn(Optional.of(existingLike));
        when(feedDislikeRepository.findByFeedIdAndGamerTagIgnoreCase(feedId, "vortex_99")).thenReturn(Optional.empty());

        FeedReactionResponse response = socialService.toggleLike(feedId, "vortex_99");

        assertFalse(response.liked());
        assertEquals(0, response.totalLikes());
        assertEquals(0, feed.getLikeCount());
        verify(feedLikeRepository, times(1)).delete(existingLike);
    }

    @Test
    void shouldToggleDislikeOnFeedItem() {
        UUID feedId = UUID.randomUUID();
        CommunityFeedEntity feed = CommunityFeedEntity.builder()
                .id(feedId)
                .likeCount(0)
                .dislikeCount(0)
                .build();

        when(communityFeedRepository.findById(feedId)).thenReturn(Optional.of(feed));
        when(feedDislikeRepository.findByFeedIdAndGamerTagIgnoreCase(feedId, "vortex_99")).thenReturn(Optional.empty());
        when(feedLikeRepository.findByFeedIdAndGamerTagIgnoreCase(feedId, "vortex_99")).thenReturn(Optional.empty());

        FeedReactionResponse response = socialService.toggleDislike(feedId, "vortex_99");

        assertFalse(response.liked());
        assertTrue(response.disliked());
        assertEquals(1, response.totalDislikes());
        assertEquals(1, feed.getDislikeCount());
        verify(feedDislikeRepository, times(1)).save(any(FeedDislikeEntity.class));
    }

    @Test
    void shouldAddCommentAndIncrementFeedCount() {
        UUID feedId = UUID.randomUUID();
        CommunityFeedEntity feed = CommunityFeedEntity.builder()
                .id(feedId)
                .commentCount(2)
                .build();

        FeedCommentRequest request = new FeedCommentRequest("valkyrie_ace", "Insane shot with Kraber!");

        when(communityFeedRepository.findById(feedId)).thenReturn(Optional.of(feed));
        when(feedCommentRepository.save(any(FeedCommentEntity.class))).thenAnswer(i -> i.getArgument(0));

        FeedCommentResponse response = socialService.addComment(feedId, request);

        assertNotNull(response);
        assertEquals("valkyrie_ace", response.gamerTag());
        assertEquals("Insane shot with Kraber!", response.commentText());
        assertEquals(3, feed.getCommentCount());
        verify(feedCommentRepository, times(1)).save(any(FeedCommentEntity.class));
    }

    @Test
    void shouldCreateManualPost() {
        CreatePostRequest request = new CreatePostRequest("vortex_99", "Apex Legends", "1v3 Clutch", "Wiped final squad", "COMBAT");

        when(communityFeedRepository.save(any(CommunityFeedEntity.class))).thenAnswer(i -> {
            CommunityFeedEntity entity = i.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        CommunityFeedItemResponse response = socialService.createManualPost(request);

        assertNotNull(response);
        assertEquals("1v3 Clutch", response.headline());
        assertEquals("vortex_99", response.authorGamerTag());
        verify(communityFeedRepository, times(1)).save(any(CommunityFeedEntity.class));
    }
}
