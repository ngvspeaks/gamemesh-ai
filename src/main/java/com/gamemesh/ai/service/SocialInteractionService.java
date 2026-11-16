package com.gamemesh.ai.service;

import com.gamemesh.ai.model.dto.*;
import com.gamemesh.ai.model.entity.*;
import com.gamemesh.ai.model.enums.SentimentScore;
import com.gamemesh.ai.model.enums.SeverityLevel;
import com.gamemesh.ai.model.enums.TelemetryCategory;
import com.gamemesh.ai.repository.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
public class SocialInteractionService {

    private final CommunityFeedRepository communityFeedRepository;
    private final FeedLikeRepository feedLikeRepository;
    private final FeedDislikeRepository feedDislikeRepository;
    private final FeedCommentRepository feedCommentRepository;

    public SocialInteractionService(
            CommunityFeedRepository communityFeedRepository,
            FeedLikeRepository feedLikeRepository,
            FeedDislikeRepository feedDislikeRepository,
            FeedCommentRepository feedCommentRepository
    ) {
        this.communityFeedRepository = communityFeedRepository;
        this.feedLikeRepository = feedLikeRepository;
        this.feedDislikeRepository = feedDislikeRepository;
        this.feedCommentRepository = feedCommentRepository;
    }

    /**
     * Seed initial realistic gaming social media posts if database is empty.
     */
    @PostConstruct
    public void seedInitialCommunityPosts() {
        if (communityFeedRepository.count() == 0) {
            log.info("Seeding initial esports social media posts into community feed...");

            UUID post1Id = UUID.randomUUID();
            UUID post2Id = UUID.randomUUID();
            UUID post3Id = UUID.randomUUID();
            UUID post4Id = UUID.randomUUID();

            List<CommunityFeedEntity> seedPosts = List.of(
                    CommunityFeedEntity.builder()
                            .id(post1Id)
                            .gameId("Apex Legends • Ranked")
                            .authorGamerTag("vortex_99")
                            .category(TelemetryCategory.COMBAT)
                            .headline("🏆 INSANE 1v4 KRABER CLUTCH in Final Ring Predator Lobby!")
                            .summary("Downed the champion squad solo with double headshots while rotating through storm. Ranked points maxed!")
                            .severity(SeverityLevel.LOW)
                            .sentiment(SentimentScore.POSITIVE)
                            .performanceImpactScore(0.0)
                            .recommendedAction("Verified clean esports highlight • Featured on Arena Feed")
                            .likeCount(342)
                            .dislikeCount(8)
                            .commentCount(2)
                            .createdAt(Instant.now().minusSeconds(3600))
                            .build(),

                    CommunityFeedEntity.builder()
                            .id(post2Id)
                            .gameId("Valorant • Competitive")
                            .authorGamerTag("valkyrie_ace")
                            .category(TelemetryCategory.CHAT)
                            .headline("⚡ Patch 10.4 Weapon Meta Discussion: Is the Operator Overpowered?")
                            .summary("Analyzed bullet economy and peekers advantage across 50 tournament rounds. Here is the full tier list breakdown.")
                            .severity(SeverityLevel.LOW)
                            .sentiment(SentimentScore.POSITIVE)
                            .performanceImpactScore(0.0)
                            .recommendedAction("Community tactical debate active")
                            .likeCount(189)
                            .dislikeCount(24)
                            .commentCount(1)
                            .createdAt(Instant.now().minusSeconds(7200))
                            .build(),

                    CommunityFeedEntity.builder()
                            .id(post3Id)
                            .gameId("CS2 • Premier")
                            .authorGamerTag("shadow_strike")
                            .category(TelemetryCategory.SYSTEM)
                            .headline("🚨 Sub-Tick Latency Anomaly & Server Lag Spike Reported in EU Central")
                            .summary("Frame pacing dropped by 45% and packet loss spiked during defuse round. Matchmaking server node undergoing diagnostic.")
                            .severity(SeverityLevel.HIGH)
                            .sentiment(SentimentScore.NEGATIVE)
                            .performanceImpactScore(0.78)
                            .recommendedAction("Escalated to server reliability team; regional failover initiated")
                            .likeCount(45)
                            .dislikeCount(67)
                            .commentCount(1)
                            .createdAt(Instant.now().minusSeconds(14400))
                            .build(),

                    CommunityFeedEntity.builder()
                            .id(post4Id)
                            .gameId("League of Legends • Ranked")
                            .authorGamerTag("toxic_demon")
                            .category(TelemetryCategory.CHAT)
                            .headline("⚠️ Automated Toxicity Strike Applied: Hate Speech & Griefing Intercepted")
                            .summary("GameMesh AI moderation shield detected toxic communication and intentional feed attempt in lobby match-eu-401.")
                            .severity(SeverityLevel.HIGH)
                            .sentiment(SentimentScore.TOXIC)
                            .performanceImpactScore(0.1)
                            .recommendedAction("72-hour ranked communication timeout issued; sportsmanship score penalized")
                            .likeCount(12)
                            .dislikeCount(156)
                            .commentCount(1)
                            .createdAt(Instant.now().minusSeconds(28800))
                            .build()
            );

            communityFeedRepository.saveAll(seedPosts);

            // Add comments to seed posts
            feedCommentRepository.saveAll(List.of(
                    FeedCommentEntity.builder()
                            .id(UUID.randomUUID())
                            .feedId(post1Id)
                            .gamerTag("valkyrie_ace")
                            .commentText("That second no-scope was absolutely disgusting! Clip of the week!")
                            .createdAt(Instant.now().minusSeconds(3200))
                            .build(),
                    FeedCommentEntity.builder()
                            .id(UUID.randomUUID())
                            .feedId(post1Id)
                            .gamerTag("shadow_strike")
                            .commentText("Predator lobby standard right there. Pure aim tracking.")
                            .createdAt(Instant.now().minusSeconds(2800))
                            .build(),
                    FeedCommentEntity.builder()
                            .id(UUID.randomUUID())
                            .feedId(post2Id)
                            .gamerTag("vortex_99")
                            .commentText("Agreed on the economy nerf. 4700 credits is too cheap for round 3 buy.")
                            .createdAt(Instant.now().minusSeconds(6500))
                            .build(),
                    FeedCommentEntity.builder()
                            .id(UUID.randomUUID())
                            .feedId(post3Id)
                            .gamerTag("valkyrie_ace")
                            .commentText("Felt that stutter too on Mirage B site. Good that watchdog caught it!")
                            .createdAt(Instant.now().minusSeconds(13000))
                            .build(),
                    FeedCommentEntity.builder()
                            .id(UUID.randomUUID())
                            .feedId(post4Id)
                            .gamerTag("vortex_99")
                            .commentText("Love to see the AI moderation shield cleaning up ranked lobbies.")
                            .createdAt(Instant.now().minusSeconds(26000))
                            .build()
            ));
        }
    }

    /**
     * Toggles like on a feed post.
     * If user previously disliked, dislike is automatically cleared.
     */
    @Transactional
    public FeedReactionResponse toggleLike(UUID feedId, String gamerTag) {
        CommunityFeedEntity feed = communityFeedRepository.findById(feedId)
                .orElseThrow(() -> new IllegalArgumentException("Feed item not found with ID: " + feedId));

        boolean wasDisliked = feedDislikeRepository.existsByFeedIdAndGamerTagIgnoreCase(feedId, gamerTag);
        if (wasDisliked) {
            feedDislikeRepository.deleteByFeedIdAndGamerTagIgnoreCase(feedId, gamerTag);
            feed.setDislikeCount(Math.max(0, feed.getDislikeCount() - 1));
        }

        Optional<FeedLikeEntity> existingLike = feedLikeRepository.findByFeedIdAndGamerTagIgnoreCase(feedId, gamerTag);
        boolean currentlyLiked;

        if (existingLike.isPresent()) {
            feedLikeRepository.delete(existingLike.get());
            feed.setLikeCount(Math.max(0, feed.getLikeCount() - 1));
            currentlyLiked = false;
        } else {
            FeedLikeEntity newLike = FeedLikeEntity.builder()
                    .id(UUID.randomUUID())
                    .feedId(feedId)
                    .gamerTag(gamerTag)
                    .createdAt(Instant.now())
                    .build();
            feedLikeRepository.save(newLike);
            feed.setLikeCount(feed.getLikeCount() + 1);
            currentlyLiked = true;
        }

        communityFeedRepository.save(feed);
        return new FeedReactionResponse(feedId, gamerTag, currentlyLiked, false, feed.getLikeCount(), feed.getDislikeCount());
    }

    /**
     * Toggles dislike on a feed post.
     * If user previously liked, like is automatically cleared.
     */
    @Transactional
    public FeedReactionResponse toggleDislike(UUID feedId, String gamerTag) {
        CommunityFeedEntity feed = communityFeedRepository.findById(feedId)
                .orElseThrow(() -> new IllegalArgumentException("Feed item not found with ID: " + feedId));

        boolean wasLiked = feedLikeRepository.existsByFeedIdAndGamerTagIgnoreCase(feedId, gamerTag);
        if (wasLiked) {
            feedLikeRepository.deleteByFeedIdAndGamerTagIgnoreCase(feedId, gamerTag);
            feed.setLikeCount(Math.max(0, feed.getLikeCount() - 1));
        }

        Optional<FeedDislikeEntity> existingDislike = feedDislikeRepository.findByFeedIdAndGamerTagIgnoreCase(feedId, gamerTag);
        boolean currentlyDisliked;

        if (existingDislike.isPresent()) {
            feedDislikeRepository.delete(existingDislike.get());
            feed.setDislikeCount(Math.max(0, feed.getDislikeCount() - 1));
            currentlyDisliked = false;
        } else {
            FeedDislikeEntity newDislike = FeedDislikeEntity.builder()
                    .id(UUID.randomUUID())
                    .feedId(feedId)
                    .gamerTag(gamerTag)
                    .createdAt(Instant.now())
                    .build();
            feedDislikeRepository.save(newDislike);
            feed.setDislikeCount(feed.getDislikeCount() + 1);
            currentlyDisliked = true;
        }

        communityFeedRepository.save(feed);
        return new FeedReactionResponse(feedId, gamerTag, false, currentlyDisliked, feed.getLikeCount(), feed.getDislikeCount());
    }

    /**
     * Manual post creation directly from the social media UI composer.
     */
    @Transactional
    public CommunityFeedItemResponse createManualPost(CreatePostRequest request) {
        CommunityFeedEntity post = CommunityFeedEntity.builder()
                .id(UUID.randomUUID())
                .gameId(request.gameId())
                .authorGamerTag(request.gamerTag())
                .category(request.category() != null ? request.category() : TelemetryCategory.CHAT)
                .headline(request.headline())
                .summary(request.content())
                .severity(SeverityLevel.LOW)
                .sentiment(SentimentScore.POSITIVE)
                .performanceImpactScore(0.0)
                .recommendedAction("User-generated social post")
                .likeCount(0)
                .dislikeCount(0)
                .commentCount(0)
                .createdAt(Instant.now())
                .build();

        CommunityFeedEntity saved = communityFeedRepository.save(post);
        return CommunityFeedItemResponse.fromEntity(saved);
    }

    /**
     * Adds a comment to an AI-curated community feed item.
     */
    @Transactional
    public FeedCommentResponse addComment(UUID feedId, FeedCommentRequest request) {
        CommunityFeedEntity feed = communityFeedRepository.findById(feedId)
                .orElseThrow(() -> new IllegalArgumentException("Feed item not found with ID: " + feedId));

        FeedCommentEntity comment = FeedCommentEntity.builder()
                .id(UUID.randomUUID())
                .feedId(feedId)
                .gamerTag(request.gamerTag())
                .commentText(request.commentText())
                .createdAt(Instant.now())
                .build();

        FeedCommentEntity saved = feedCommentRepository.save(comment);

        feed.setCommentCount(feed.getCommentCount() + 1);
        communityFeedRepository.save(feed);

        return FeedCommentResponse.fromEntity(saved);
    }

    /**
     * Retrieves all comments on a feed item in chronological order.
     */
    @Transactional(readOnly = true)
    public List<FeedCommentResponse> getComments(UUID feedId) {
        return feedCommentRepository.findByFeedIdOrderByCreatedAtAsc(feedId)
                .stream()
                .map(FeedCommentResponse::fromEntity)
                .toList();
    }
}
