package com.gamemesh.ai;

import com.gamemesh.ai.model.dto.BatchTelemetryIngestResponse;
import com.gamemesh.ai.model.dto.TelemetryIngestRequest;
import com.gamemesh.ai.model.dto.TelemetryIngestResponse;
import com.gamemesh.ai.model.dto.TelemetryMessage;
import com.gamemesh.ai.model.enums.TelemetryCategory;
import com.gamemesh.ai.service.AmqpProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AmqpProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private AmqpProducer amqpProducer;

    @BeforeEach
    void setUp() {
        amqpProducer = new AmqpProducer(rabbitTemplate);
        ReflectionTestUtils.setField(amqpProducer, "exchange", "gamemesh.telemetry.exchange");
        ReflectionTestUtils.setField(amqpProducer, "routingKeyPrefix", "telemetry.event.");
    }

    @Test
    void shouldPublishSingleTelemetryEventWithCorrectRoutingKey() {
        TelemetryIngestRequest request = new TelemetryIngestRequest(
                "game-101",
                "player-ace",
                TelemetryCategory.COMBAT,
                "PLAYER_ELIMINATION",
                "{\"target\":\"player-2\"}",
                Instant.now()
        );

        TelemetryIngestResponse response = amqpProducer.publishTelemetry(request);

        assertNotNull(response.eventId());
        assertEquals("ACCEPTED", response.status());

        ArgumentCaptor<TelemetryMessage> messageCaptor = ArgumentCaptor.forClass(TelemetryMessage.class);
        ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);

        verify(rabbitTemplate, times(1)).convertAndSend(
                eq("gamemesh.telemetry.exchange"),
                routingKeyCaptor.capture(),
                messageCaptor.capture(),
                any(MessagePostProcessor.class)
        );

        assertEquals("telemetry.event.combat", routingKeyCaptor.getValue());
        assertEquals("game-101", messageCaptor.getValue().gameId());
        assertEquals(TelemetryCategory.COMBAT, messageCaptor.getValue().category());
    }

    @Test
    void shouldPublishBatchOfHighFrequencyTelemetryEvents() {
        List<TelemetryIngestRequest> batch = List.of(
                new TelemetryIngestRequest("g1", "p1", TelemetryCategory.CHAT, "CHAT_MSG", "gg", Instant.now()),
                new TelemetryIngestRequest("g1", "p2", TelemetryCategory.ECONOMY, "PURCHASE", "shield", Instant.now()),
                new TelemetryIngestRequest("g1", "p3", TelemetryCategory.SYSTEM, "TICK", "120fps", Instant.now())
        );

        BatchTelemetryIngestResponse batchResponse = amqpProducer.publishBatch(batch);

        assertEquals(3, batchResponse.totalAccepted());
        assertEquals(3, batchResponse.eventIds().size());
        assertEquals("ACCEPTED", batchResponse.status());

        verify(rabbitTemplate, times(3)).convertAndSend(
                eq("gamemesh.telemetry.exchange"),
                any(String.class),
                any(TelemetryMessage.class),
                any(MessagePostProcessor.class)
        );
    }
}

