package com.gamemesh.ai.service;

import com.gamemesh.ai.model.dto.BatchTelemetryIngestResponse;
import com.gamemesh.ai.model.dto.TelemetryIngestRequest;
import com.gamemesh.ai.model.dto.TelemetryIngestResponse;
import com.gamemesh.ai.model.dto.TelemetryMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class AmqpProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${gamemesh.rabbitmq.exchange:gamemesh.telemetry.exchange}")
    private String exchange;

    @Value("${gamemesh.rabbitmq.routing-key-prefix:telemetry.event.}")
    private String routingKeyPrefix;

    public AmqpProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Publishes a single incoming telemetry event to the RabbitMQ exchange.
     */
    public TelemetryIngestResponse publishTelemetry(TelemetryIngestRequest request) {
        UUID eventId = UUID.randomUUID();
        String routingKey = routingKeyPrefix + request.category().name().toLowerCase();

        TelemetryMessage message = new TelemetryMessage(
                eventId,
                request.gameId(),
                request.playerId(),
                request.category(),
                request.eventType(),
                request.payload(),
                request.timestamp()
        );

        log.info("AMQP Producer -> Dispatching event [{}] category '{}' to exchange '{}' with routingKey '{}'",
                eventId, request.category(), exchange, routingKey);

        rabbitTemplate.convertAndSend(exchange, routingKey, message, m -> {
            m.getMessageProperties().setCorrelationId(eventId.toString());
            m.getMessageProperties().setHeader("X-Game-Id", request.gameId());
            m.getMessageProperties().setHeader("X-Player-Id", request.playerId());
            m.getMessageProperties().setHeader("X-Category", request.category().name());
            m.getMessageProperties().setHeader("X-Published-EpochMs", Instant.now().toEpochMilli());
            return m;
        });

        return TelemetryIngestResponse.accepted(eventId);
    }

    /**
     * Publishes a high-frequency batch of telemetry payloads in sequence.
     */
    public BatchTelemetryIngestResponse publishBatch(List<TelemetryIngestRequest> requests) {
        long startTime = System.currentTimeMillis();
        List<UUID> eventIds = new ArrayList<>(requests.size());

        for (TelemetryIngestRequest request : requests) {
            TelemetryIngestResponse singleResponse = publishTelemetry(request);
            eventIds.add(singleResponse.eventId());
        }

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("AMQP Producer -> Dispatched high-frequency burst of {} events in {}ms ({} events/sec)",
                requests.size(), elapsed, elapsed > 0 ? (requests.size() * 1000L / elapsed) : requests.size() * 1000L);

        return BatchTelemetryIngestResponse.accepted(eventIds, elapsed);
    }
}

