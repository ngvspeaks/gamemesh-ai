package com.gamemesh.ai;

import com.gamemesh.ai.model.dto.CreateProfileRequest;
import com.gamemesh.ai.model.dto.UserProfileResponse;
import com.gamemesh.ai.model.entity.UserFollowEntity;
import com.gamemesh.ai.model.entity.UserProfileEntity;
import com.gamemesh.ai.repository.UserFollowRepository;
import com.gamemesh.ai.repository.UserProfileRepository;
import com.gamemesh.ai.service.UserProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private UserFollowRepository userFollowRepository;

    private UserProfileService profileService;

    @BeforeEach
    void setUp() {
        profileService = new UserProfileService(userProfileRepository, userFollowRepository);
    }

    @Test
    void shouldCreateNewUserProfile() {
        CreateProfileRequest request = new CreateProfileRequest(
                "reaper_x",
                "Reaper X",
                "Apex Predator",
                null,
                "Diamond I",
                "Apex Legends"
        );

        when(userProfileRepository.existsByGamerTagIgnoreCase("reaper_x")).thenReturn(false);
        when(userProfileRepository.save(any(UserProfileEntity.class))).thenAnswer(i -> i.getArgument(0));

        UserProfileResponse response = profileService.createProfile(request);

        assertNotNull(response);
        assertEquals("reaper_x", response.gamerTag());
        assertEquals(100, response.reputationScore());
        verify(userProfileRepository, times(1)).save(any(UserProfileEntity.class));
    }

    @Test
    void shouldThrowExceptionWhenGamerTagAlreadyExists() {
        CreateProfileRequest request = new CreateProfileRequest(
                "vortex_99",
                "Vortex Clone",
                null, null, null, null
        );

        when(userProfileRepository.existsByGamerTagIgnoreCase("vortex_99")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> profileService.createProfile(request));
        verify(userProfileRepository, never()).save(any(UserProfileEntity.class));
    }

    @Test
    void shouldFollowAnotherGamerSuccessfully() {
        UserProfileEntity target = UserProfileEntity.builder()
                .id(UUID.randomUUID())
                .gamerTag("valkyrie_ace")
                .followersCount(10)
                .build();

        UserProfileEntity actor = UserProfileEntity.builder()
                .id(UUID.randomUUID())
                .gamerTag("vortex_99")
                .followingCount(5)
                .build();

        when(userFollowRepository.existsByFollowerTagIgnoreCaseAndFollowingTagIgnoreCase("vortex_99", "valkyrie_ace"))
                .thenReturn(false);
        when(userProfileRepository.findByGamerTagIgnoreCase("valkyrie_ace")).thenReturn(Optional.of(target));
        when(userProfileRepository.findByGamerTagIgnoreCase("vortex_99")).thenReturn(Optional.of(actor));

        boolean followed = profileService.followUser("vortex_99", "valkyrie_ace");

        assertTrue(followed);
        assertEquals(11, target.getFollowersCount());
        assertEquals(6, actor.getFollowingCount());
        verify(userFollowRepository, times(1)).save(any(UserFollowEntity.class));
    }
}

