package com.gamemesh.ai.service;

import com.gamemesh.ai.exception.TelemetryProcessingException;
import com.gamemesh.ai.model.dto.TelemetryMessage;
import com.gamemesh.ai.model.entity.CommunityFeedEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class AmqpListener {

    private final AgentService agentService;
    private final AtomicLong processedCount = new AtomicLong(0);

    public AmqpListener(AgentService agentService) {
        this.agentService = agentService;
    }

    /**
     * Intercepts consumed telemetry messages from the primary RabbitMQ queue.
     * Simulates real-time background processing by delegating to AgentService
     * for persistence and AI curation.
     */
    @RabbitListener(
            queues = "${gamemesh.rabbitmq.queue:gamemesh.telemetry.queue}",
            containerFactory = "rabbitListenerContainerFactory"
    )
    @Transactional
    public void onTelemetryMessage(TelemetryMessage message, Message rawAmqpMessage) {
        long currentCount = processedCount.incrementAndGet();
        Instant receivedTime = Instant.now();
        long transitLatencyMs = message.timestamp() != null
                ? Duration.between(message.timestamp(), receivedTime).toMillis()
                : 0;

        log.info("AMQP Listener [Seq #{}] <- Inbound event [{}] | Game: {} | Player: {} | Category: {} | Transit Latency: {}ms",
                currentCount, message.eventId(), message.gameId(), message.playerId(), message.category(), transitLatencyMs);

        // 1. Error simulation hook: test retry attempts and dead-letter queue routing
        if (message.payload() != null && message.payload().contains("SIMULATE_FAILURE")) {
            log.warn("AMQP Listener -> Intentionally throwing TelemetryProcessingException for event [{}] to trigger retry policy",
                    message.eventId());
            throw new TelemetryProcessingException("Simulated processing failure for telemetry event: " + message.eventId());
        }

        // 2. Delegate to AgentService to persist raw telemetry & generate AI-curated digest
        CommunityFeedEntity feedItem = agentService.processTelemetryEvent(message);

        log.debug("AMQP Listener -> Interception complete for event [{}] - Feed headline: '{}'",
                message.eventId(), feedItem.getHeadline());
    }

    /**
     * Dead-Letter Queue (DLQ) listener to observe, log, and audit exhausted/failed messages.
     */
    @RabbitListener(queues = "${gamemesh.rabbitmq.dlq:gamemesh.telemetry.dlq}")
    public void onDeadLetterMessage(Message failedMessage) {
        log.error("💀 AMQP DLQ Listener <- Received dead-lettered message! CorrelationId: {}, DeathReason: {}, Headers: {}",
                failedMessage.getMessageProperties().getCorrelationId(),
                failedMessage.getMessageProperties().getXDeathHeader(),
                failedMessage.getMessageProperties().getHeaders());
    }

    public long getProcessedCount() {
        return processedCount.get();
    }
}
