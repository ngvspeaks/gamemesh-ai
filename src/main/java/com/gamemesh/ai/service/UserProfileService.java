package com.gamemesh.ai.service;

import com.gamemesh.ai.model.dto.CreateProfileRequest;
import com.gamemesh.ai.model.dto.UserProfileResponse;
import com.gamemesh.ai.model.entity.UserFollowEntity;
import com.gamemesh.ai.model.entity.UserProfileEntity;
import com.gamemesh.ai.repository.UserFollowRepository;
import com.gamemesh.ai.repository.UserProfileRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserFollowRepository userFollowRepository;

    public UserProfileService(UserProfileRepository userProfileRepository, UserFollowRepository userFollowRepository) {
        this.userProfileRepository = userProfileRepository;
        this.userFollowRepository = userFollowRepository;
    }

    @PostConstruct
    public void seedInitialEsportsGamers() {
        if (userProfileRepository.count() == 0) {
            log.info("Seeding initial esports gamer profiles into GameMesh AI social network...");
            List<UserProfileEntity> initialProfiles = List.of(
                    UserProfileEntity.builder()
                            .id(UUID.randomUUID())
                            .gamerTag("vortex_99")
                            .displayName("Vortex Prime")
                            .avatarUrl("https://api.dicebear.com/7.x/bottts/svg?seed=vortex_99")
                            .bio("Pro Apex Legends & Valorant Initiator. Clutch or kick.")
                            .rankTier("Apex Predator #14")
                            .reputationScore(100)
                            .followersCount(1420)
                            .followingCount(85)
                            .favoriteGame("Apex Legends")
                            .createdAt(Instant.now())
                            .build(),
                    UserProfileEntity.builder()
                            .id(UUID.randomUUID())
                            .gamerTag("valkyrie_ace")
                            .displayName("Valkyrie Ace")
                            .avatarUrl("https://api.dicebear.com/7.x/bottts/svg?seed=valkyrie_ace")
                            .bio("Esports champion & flight simulator captain. Streamer & community host.")
                            .rankTier("Radiant Tier 1")
                            .reputationScore(98)
                            .followersCount(3100)
                            .followingCount(120)
                            .favoriteGame("Valorant")
                            .createdAt(Instant.now())
                            .build(),
                    UserProfileEntity.builder()
                            .id(UUID.randomUUID())
                            .gamerTag("shadow_strike")
                            .displayName("Shadow Strike")
                            .avatarUrl("https://api.dicebear.com/7.x/bottts/svg?seed=shadow_strike")
                            .bio("Sniper main. Sector control and high-accuracy recon.")
                            .rankTier("Master II")
                            .reputationScore(95)
                            .followersCount(890)
                            .followingCount(40)
                            .favoriteGame("Counter-Strike 2")
                            .createdAt(Instant.now())
                            .build(),
                    UserProfileEntity.builder()
                            .id(UUID.randomUUID())
                            .gamerTag("toxic_demon")
                            .displayName("Toxic Demon")
                            .avatarUrl("https://api.dicebear.com/7.x/bottts/svg?seed=toxic_demon")
                            .bio("Rage gamer. Reformed feeder.")
                            .rankTier("Gold III")
                            .reputationScore(35) // Flagged low reputation
                            .followersCount(12)
                            .followingCount(4)
                            .favoriteGame("League of Legends")
                            .createdAt(Instant.now())
                            .build()
            );
            userProfileRepository.saveAll(initialProfiles);
        }
    }

    @Transactional
    public UserProfileResponse createProfile(CreateProfileRequest request) {
        if (userProfileRepository.existsByGamerTagIgnoreCase(request.gamerTag())) {
            throw new IllegalArgumentException("GamerTag '" + request.gamerTag() + "' is already registered!");
        }

        UserProfileEntity entity = UserProfileEntity.builder()
                .id(UUID.randomUUID())
                .gamerTag(request.gamerTag())
                .displayName(request.displayName())
                .avatarUrl(request.avatarUrl())
                .bio(request.bio())
                .rankTier(request.rankTier() != null ? request.rankTier() : "Rookie I")
                .favoriteGame(request.favoriteGame() != null ? request.favoriteGame() : "All")
                .reputationScore(100)
                .followersCount(0)
                .followingCount(0)
                .createdAt(Instant.now())
                .build();

        UserProfileEntity saved = userProfileRepository.save(entity);
        return UserProfileResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public Optional<UserProfileResponse> getProfileByGamerTag(String gamerTag) {
        return userProfileRepository.findByGamerTagIgnoreCase(gamerTag)
                .map(UserProfileResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<UserProfileResponse> getTopProfiles(int limit) {
        int bounded = Math.clamp(limit, 1, 50);
        return userProfileRepository.findAllByOrderByReputationScoreDesc(PageRequest.of(0, bounded))
                .stream()
                .map(UserProfileResponse::fromEntity)
                .toList();
    }

    @Transactional
    public boolean followUser(String followerTag, String followingTag) {
        if (followerTag.equalsIgnoreCase(followingTag)) {
            throw new IllegalArgumentException("You cannot follow yourself!");
        }

        if (userFollowRepository.existsByFollowerTagIgnoreCaseAndFollowingTagIgnoreCase(followerTag, followingTag)) {
            return false; // already following
        }

        UserFollowEntity follow = UserFollowEntity.builder()
                .id(UUID.randomUUID())
                .followerTag(followerTag)
                .followingTag(followingTag)
                .followedAt(Instant.now())
                .build();
        userFollowRepository.save(follow);

        // Update counts
        userProfileRepository.findByGamerTagIgnoreCase(followingTag).ifPresent(target -> {
            target.setFollowersCount(target.getFollowersCount() + 1);
            userProfileRepository.save(target);
        });
        userProfileRepository.findByGamerTagIgnoreCase(followerTag).ifPresent(actor -> {
            actor.setFollowingCount(actor.getFollowingCount() + 1);
            userProfileRepository.save(actor);
        });

        return true;
    }

    @Transactional
    public boolean unfollowUser(String followerTag, String followingTag) {
        Optional<UserFollowEntity> follow = userFollowRepository.findByFollowerTagIgnoreCaseAndFollowingTagIgnoreCase(followerTag, followingTag);
        if (follow.isPresent()) {
            userFollowRepository.delete(follow.get());

            userProfileRepository.findByGamerTagIgnoreCase(followingTag).ifPresent(target -> {
                target.setFollowersCount(Math.max(0, target.getFollowersCount() - 1));
                userProfileRepository.save(target);
            });
            userProfileRepository.findByGamerTagIgnoreCase(followerTag).ifPresent(actor -> {
                actor.setFollowingCount(Math.max(0, actor.getFollowingCount() - 1));
                userProfileRepository.save(actor);
            });
            return true;
        }
        return false;
    }
}

