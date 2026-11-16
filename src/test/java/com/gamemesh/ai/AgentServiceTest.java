package com.gamemesh.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamemesh.ai.model.dto.TelemetryMessage;
import com.gamemesh.ai.model.entity.CommunityFeedEntity;
import com.gamemesh.ai.model.entity.TelemetryEventEntity;
import com.gamemesh.ai.model.enums.SentimentScore;
import com.gamemesh.ai.model.enums.SeverityLevel;
import com.gamemesh.ai.model.enums.TelemetryCategory;
import com.gamemesh.ai.repository.CommunityFeedRepository;
import com.gamemesh.ai.repository.CuratedInsightRepository;
import com.gamemesh.ai.repository.TelemetryEventRepository;
import com.gamemesh.ai.service.AgentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgentServiceTest {

    @Mock
    private TelemetryEventRepository telemetryEventRepository;

    @Mock
    private CommunityFeedRepository communityFeedRepository;

    @Mock
    private CuratedInsightRepository curatedInsightRepository;

    private AgentService agentService;

    @BeforeEach
    void setUp() {
        agentService = new AgentService(
                Optional.empty(),
                new ObjectMapper(),
                telemetryEventRepository,
                communityFeedRepository,
                curatedInsightRepository
        );
    }

    @Test
    void shouldPersistRawTelemetryAndGenerateCriticalAlertForErrorLogs() {
        UUID eventId = UUID.randomUUID();
        TelemetryMessage errorMessage = new TelemetryMessage(
                eventId,
                "game-crash-99",
                "player-victim",
                TelemetryCategory.SYSTEM,
                "ENGINE_CRASH",
                "FATAL NullPointerException in physics tick: Null pointer dereference on rigid body",
                Instant.now()
        );

        when(communityFeedRepository.save(any(CommunityFeedEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CommunityFeedEntity result = agentService.processTelemetryEvent(errorMessage);

        assertNotNull(result);
        assertEquals(SeverityLevel.CRITICAL, result.getSeverity());
        assertEquals(SentimentScore.NEGATIVE, result.getSentiment());
        assertTrue(result.getPerformanceImpactScore() >= 0.9);
        assertTrue(result.getHeadline().contains("Critical Error"));

        // Verify raw telemetry was persisted
        ArgumentCaptor<TelemetryEventEntity> rawCaptor = ArgumentCaptor.forClass(TelemetryEventEntity.class);
        verify(telemetryEventRepository, times(1)).save(rawCaptor.capture());
        assertEquals(eventId, rawCaptor.getValue().getId());
        assertEquals("game-crash-99", rawCaptor.getValue().getGameId());

        // Verify community feed was persisted
        verify(communityFeedRepository, times(1)).save(any(CommunityFeedEntity.class));
    }

    @Test
    void shouldDetectPerformanceSpikesAndGenerateAlert() {
        UUID eventId = UUID.randomUUID();
        TelemetryMessage perfMessage = new TelemetryMessage(
                eventId,
                "game-lag-01",
                "player-stutter",
                TelemetryCategory.SYSTEM,
                "PERFORMANCE_METRICS",
                "{\"serverFps\":14,\"pingMs\":280,\"packetloss\":12.4,\"stutter\":true}",
                Instant.now()
        );

        when(communityFeedRepository.save(any(CommunityFeedEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CommunityFeedEntity result = agentService.processTelemetryEvent(perfMessage);

        assertNotNull(result);
        assertEquals(SeverityLevel.HIGH, result.getSeverity());
        assertTrue(result.getHeadline().contains("Performance Alert"));
        assertTrue(result.getPerformanceImpactScore() > 0.5);

        verify(telemetryEventRepository, times(1)).save(any(TelemetryEventEntity.class));
        verify(communityFeedRepository, times(1)).save(any(CommunityFeedEntity.class));
    }

    @Test
    void shouldCapturePositiveSportsmanshipInCommunityFeed() {
        UUID eventId = UUID.randomUUID();
        TelemetryMessage ggMessage = new TelemetryMessage(
                eventId,
                "game-match-77",
                "player-leader",
                TelemetryCategory.CHAT,
                "CHAT_MESSAGE",
                "gg wp everyone! Incredible clutch comeback!",
                Instant.now()
        );

        when(communityFeedRepository.save(any(CommunityFeedEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CommunityFeedEntity result = agentService.processTelemetryEvent(ggMessage);

        assertNotNull(result);
        assertEquals(SeverityLevel.LOW, result.getSeverity());
        assertEquals(SentimentScore.POSITIVE, result.getSentiment());
        assertEquals(0.0, result.getPerformanceImpactScore());
        assertTrue(result.getHeadline().contains("Sportsmanship"));
    }
}

