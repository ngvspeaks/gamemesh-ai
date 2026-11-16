package com.gamemesh.ai.controller;

import com.gamemesh.ai.model.dto.CreateProfileRequest;
import com.gamemesh.ai.model.dto.UserProfileResponse;
import com.gamemesh.ai.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/profiles")
public class ProfileController {

    private final UserProfileService userProfileService;

    public ProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    /**
     * Register / create a new gamer profile.
     */
    @PostMapping
    public ResponseEntity<UserProfileResponse> createProfile(@Valid @RequestBody CreateProfileRequest request) {
        UserProfileResponse profile = userProfileService.createProfile(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(profile);
    }

    /**
     * Retrieve gamer profile by GamerTag.
     */
    @GetMapping("/{gamerTag}")
    public ResponseEntity<UserProfileResponse> getProfile(@PathVariable String gamerTag) {
        return userProfileService.getProfileByGamerTag(gamerTag)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get top gamers / leaderboard sorted by reputation.
     */
    @GetMapping("/leaderboard")
    public ResponseEntity<List<UserProfileResponse>> getLeaderboard(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(userProfileService.getTopProfiles(limit));
    }

    /**
     * Follow another gamer.
     */
    @PostMapping("/{followingTag}/follow")
    public ResponseEntity<Map<String, Object>> followUser(
            @PathVariable String followingTag,
            @RequestParam String followerTag
    ) {
        boolean followed = userProfileService.followUser(followerTag, followingTag);
        return ResponseEntity.ok(Map.of(
                "follower", followerTag,
                "following", followingTag,
                "success", followed,
                "message", followed ? "Successfully followed gamer" : "Already following this gamer"
        ));
    }

    /**
     * Unfollow a gamer.
     */
    @DeleteMapping("/{followingTag}/follow")
    public ResponseEntity<Map<String, Object>> unfollowUser(
            @PathVariable String followingTag,
            @RequestParam String followerTag
    ) {
        boolean unfollowed = userProfileService.unfollowUser(followerTag, followingTag);
        return ResponseEntity.ok(Map.of(
                "follower", followerTag,
                "following", followingTag,
                "success", unfollowed,
                "message", unfollowed ? "Successfully unfollowed gamer" : "Not following this gamer"
        ));
    }
}

