package com.gamemesh.ai;

import com.gamemesh.ai.exception.TelemetryProcessingException;
import com.gamemesh.ai.model.dto.TelemetryMessage;
import com.gamemesh.ai.model.entity.CommunityFeedEntity;
import com.gamemesh.ai.model.enums.SentimentScore;
import com.gamemesh.ai.model.enums.SeverityLevel;
import com.gamemesh.ai.model.enums.TelemetryCategory;
import com.gamemesh.ai.service.AgentService;
import com.gamemesh.ai.service.AmqpListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AmqpListenerTest {

    @Mock
    private AgentService agentService;

    private AmqpListener amqpListener;

    @BeforeEach
    void setUp() {
        amqpListener = new AmqpListener(agentService);
    }

    @Test
    void shouldSuccessfullyInterceptAndProcessTelemetryMessage() {
        UUID eventId = UUID.randomUUID();
        TelemetryMessage message = new TelemetryMessage(
                eventId,
                "game-42",
                "player-pro",
                TelemetryCategory.CHAT,
                "CHAT_MESSAGE",
                "gg wp great teamplay",
                Instant.now()
        );

        CommunityFeedEntity mockFeed = CommunityFeedEntity.builder()
                .id(UUID.randomUUID())
                .telemetryEventId(eventId)
                .gameId("game-42")
                .headline("Match Highlight")
                .summary("Great teamplay")
                .severity(SeverityLevel.LOW)
                .sentiment(SentimentScore.POSITIVE)
                .performanceImpactScore(0.0)
                .build();

        when(agentService.processTelemetryEvent(any(TelemetryMessage.class))).thenReturn(mockFeed);

        Message rawAmqpMessage = new Message("{}".getBytes(), new MessageProperties());

        amqpListener.onTelemetryMessage(message, rawAmqpMessage);

        verify(agentService, times(1)).processTelemetryEvent(message);
        assertEquals(1, amqpListener.getProcessedCount());
    }

    @Test
    void shouldThrowTelemetryProcessingExceptionWhenFailureTriggerIsPresent() {
        UUID eventId = UUID.randomUUID();
        TelemetryMessage poisonMessage = new TelemetryMessage(
                eventId,
                "game-42",
                "player-poison",
                TelemetryCategory.SYSTEM,
                "CORRUPT_PACKET",
                "SIMULATE_FAILURE - poison pill test",
                Instant.now()
        );

        Message rawAmqpMessage = new Message("{}".getBytes(), new MessageProperties());

        assertThrows(TelemetryProcessingException.class, () ->
                amqpListener.onTelemetryMessage(poisonMessage, rawAmqpMessage)
        );

        verify(agentService, never()).processTelemetryEvent(any(TelemetryMessage.class));
    }
}
