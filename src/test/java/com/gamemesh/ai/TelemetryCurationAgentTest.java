package com.gamemesh.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamemesh.ai.agent.TelemetryCurationAgent;
import com.gamemesh.ai.model.dto.TelemetryMessage;
import com.gamemesh.ai.model.entity.CuratedInsightEntity;
import com.gamemesh.ai.model.enums.SentimentScore;
import com.gamemesh.ai.model.enums.TelemetryCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TelemetryCurationAgentTest {

    private TelemetryCurationAgent agent;

    @BeforeEach
    void setUp() {
        agent = new TelemetryCurationAgent(Optional.empty(), new ObjectMapper());
    }

    @Test
    void shouldDetectToxicLanguageInChat() {
        TelemetryMessage message = new TelemetryMessage(
                UUID.randomUUID(),
                "game-42",
                "player-toxic",
                TelemetryCategory.CHAT,
                "CHAT_MESSAGE",
                "you are complete trash feeder uninstall now",
                Instant.now()
        );

        CuratedInsightEntity insight = agent.analyze(message);

        assertEquals(SentimentScore.TOXIC, insight.getSentiment());
        assertTrue(insight.isFlaggedToxicity());
        assertTrue(insight.getToxicityScore() >= 0.7);
    }

    @Test
    void shouldDetectPositiveSportsmanship() {
        TelemetryMessage message = new TelemetryMessage(
                UUID.randomUUID(),
                "game-42",
                "player-friendly",
                TelemetryCategory.CHAT,
                "CHAT_MESSAGE",
                "gg wp great teamplay guys",
                Instant.now()
        );

        CuratedInsightEntity insight = agent.analyze(message);

        assertEquals(SentimentScore.POSITIVE, insight.getSentiment());
        assertFalse(insight.isFlaggedToxicity());
        assertTrue(insight.getToxicityScore() < 0.2);
    }

    @Test
    void shouldHandleNeutralSystemEvents() {
        TelemetryMessage message = new TelemetryMessage(
                UUID.randomUUID(),
                "game-42",
                "player-1",
                TelemetryCategory.SYSTEM,
                "ZONE_TRANSITION",
                "Player moved from sector A to sector B",
                Instant.now()
        );

        CuratedInsightEntity insight = agent.analyze(message);

        assertEquals(SentimentScore.NEUTRAL, insight.getSentiment());
        assertFalse(insight.isFlaggedToxicity());
    }
}

