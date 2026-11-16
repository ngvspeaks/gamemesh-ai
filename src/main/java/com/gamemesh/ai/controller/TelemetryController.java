package com.gamemesh.ai.controller;

import com.gamemesh.ai.model.dto.BatchTelemetryIngestRequest;
import com.gamemesh.ai.model.dto.BatchTelemetryIngestResponse;
import com.gamemesh.ai.model.dto.TelemetryIngestRequest;
import com.gamemesh.ai.model.dto.TelemetryIngestResponse;
import com.gamemesh.ai.model.entity.TelemetryEventEntity;
import com.gamemesh.ai.model.enums.TelemetryCategory;
import com.gamemesh.ai.service.AmqpProducer;
import com.gamemesh.ai.service.InsightQueryService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/telemetry")
public class TelemetryController {

    private final AmqpProducer amqpProducer;
    private final InsightQueryService queryService;

    private static final List<String> MOCK_PLAYERS = List.of(
            "vortex_99", "shadow_strike", "valkyrie_ace", "neon_phantom", "toxic_demon", "zenith_god"
    );

    private static final List<String> MOCK_CHAT_MESSAGES = List.of(
            "gg wp all! Fantastic defense in sector 4!",
            "Need backup at objective B now!",
            "you are complete trash feeder uninstall the game now idiot",
            "Thanks for the heal support, clutch save!",
            "Stop griefing and play the objective please",
            "Buying sniper rifle and pushing long corridor"
    );

    public TelemetryController(AmqpProducer amqpProducer, InsightQueryService queryService) {
        this.amqpProducer = amqpProducer;
        this.queryService = queryService;
    }

    /**
     * Ingest a single telemetry event, enqueuing it immediately onto RabbitMQ.
     */
    @PostMapping
    public ResponseEntity<TelemetryIngestResponse> ingestTelemetry(@Valid @RequestBody TelemetryIngestRequest request) {
        TelemetryIngestResponse response = amqpProducer.publishTelemetry(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    /**
     * Ingest a high-frequency batch of telemetry payloads in a single HTTP call.
     */
    @PostMapping("/batch")
    public ResponseEntity<BatchTelemetryIngestResponse> ingestBatch(@Valid @RequestBody BatchTelemetryIngestRequest request) {
        BatchTelemetryIngestResponse response = amqpProducer.publishBatch(request.events());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    /**
     * Simulation endpoint to generate and dispatch a high-frequency burst of mock game telemetry events.
     * Useful for load-testing, throughput verification, and monitoring queue behavior.
     */
    @PostMapping("/mock/simulate")
    public ResponseEntity<BatchTelemetryIngestResponse> simulateHighFrequencyMockTelemetry(
            @RequestParam(defaultValue = "25") int count,
            @RequestParam(required = false) String gameId
    ) {
        int boundedCount = Math.clamp(count, 1, 500);
        String targetGameId = (gameId != null && !gameId.isBlank()) ? gameId : "match-" + UUID.randomUUID().toString().substring(0, 8);

        List<TelemetryIngestRequest> mockEvents = new ArrayList<>(boundedCount);
        Random random = new Random();

        for (int i = 0; i < boundedCount; i++) {
            String player = MOCK_PLAYERS.get(random.nextInt(MOCK_PLAYERS.size()));
            TelemetryCategory category = TelemetryCategory.values()[random.nextInt(TelemetryCategory.values().length)];

            record MockData(String eventType, String payload) {}

            MockData mockData = switch (category) {
                case CHAT -> new MockData(
                        "CHAT_MESSAGE",
                        MOCK_CHAT_MESSAGES.get(random.nextInt(MOCK_CHAT_MESSAGES.size()))
                );
                case COMBAT -> new MockData(
                        random.nextBoolean() ? "PLAYER_ELIMINATION" : "DAMAGE_DEALT",
                        String.format("{\"weapon\":\"plasma-rifle\",\"damage\":%d,\"headshot\":%b,\"x\":%d,\"y\":%d}",
                                random.nextInt(120) + 10, random.nextBoolean(), random.nextInt(1000), random.nextInt(1000))
                );
                case ECONOMY -> new MockData(
                        "ITEM_PURCHASE",
                        String.format("{\"item\":\"shield-battery\",\"cost\":%d,\"remainingCredits\":%d}",
                                random.nextInt(500) + 50, random.nextInt(2000))
                );
                case SYSTEM -> new MockData(
                        "TICK_UPDATE",
                        String.format("{\"serverFps\":%d,\"pingMs\":%d,\"packetLoss\":0.0}",
                                random.nextInt(10) + 120, random.nextInt(40) + 15)
                );
            };

            mockEvents.add(new TelemetryIngestRequest(
                    targetGameId,
                    player,
                    category,
                    mockData.eventType(),
                    mockData.payload(),
                    Instant.now()
            ));
        }

        BatchTelemetryIngestResponse response = amqpProducer.publishBatch(mockEvents);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    /**
     * Endpoint to simulate a poison message / consumption failure to verify retry logic and Dead Letter Queue routing.
     */
    @PostMapping("/mock/failure")
    public ResponseEntity<TelemetryIngestResponse> simulateConsumptionFailure(
            @RequestParam(defaultValue = "match-fail-test") String gameId
    ) {
        TelemetryIngestRequest poisonRequest = new TelemetryIngestRequest(
                gameId,
                "player-poison",
                TelemetryCategory.SYSTEM,
                "CORRUPT_PACKET",
                "SIMULATE_FAILURE - Intentional payload trigger to test retry exhaustion and DLQ routing",
                Instant.now()
        );

        TelemetryIngestResponse response = amqpProducer.publishTelemetry(poisonRequest);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    /**
     * Retrieve telemetry history for a specific game instance.
     */
    @GetMapping("/games/{gameId}")
    public ResponseEntity<List<TelemetryEventEntity>> getGameTelemetry(@PathVariable String gameId) {
        List<TelemetryEventEntity> events = queryService.getEventsForGame(gameId);
        return ResponseEntity.ok(events);
    }
}
