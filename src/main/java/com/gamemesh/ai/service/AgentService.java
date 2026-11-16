package com.gamemesh.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamemesh.ai.agent.PromptTemplates;
import com.gamemesh.ai.model.dto.CommunityDigest;
import com.gamemesh.ai.model.dto.TelemetryMessage;
import com.gamemesh.ai.model.entity.CommunityFeedEntity;
import com.gamemesh.ai.model.entity.CuratedInsightEntity;
import com.gamemesh.ai.model.entity.TelemetryEventEntity;
import com.gamemesh.ai.model.enums.SentimentScore;
import com.gamemesh.ai.model.enums.SeverityLevel;
import com.gamemesh.ai.repository.CommunityFeedRepository;
import com.gamemesh.ai.repository.CuratedInsightRepository;
import com.gamemesh.ai.repository.TelemetryEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
public class AgentService {

    private final Optional<ChatClient> chatClient;
    private final ObjectMapper objectMapper;
    private final TelemetryEventRepository telemetryEventRepository;
    private final CommunityFeedRepository communityFeedRepository;
    private final CuratedInsightRepository curatedInsightRepository;

    @Value("${gamemesh.ai.provider:fallback}")
    private String configuredProvider;

    public AgentService(
            Optional<ChatClient> chatClient,
            ObjectMapper objectMapper,
            TelemetryEventRepository telemetryEventRepository,
            CommunityFeedRepository communityFeedRepository,
            CuratedInsightRepository curatedInsightRepository
    ) {
        this.chatClient = chatClient;
        this.objectMapper = objectMapper;
        this.telemetryEventRepository = telemetryEventRepository;
        this.communityFeedRepository = communityFeedRepository;
        this.curatedInsightRepository = curatedInsightRepository;
    }

    /**
     * Intercepts consumed telemetry event from the queue, persists raw data,
     * executes Spring AI curation, and stores the structured community digest.
     */
    @Transactional
    public CommunityFeedEntity processTelemetryEvent(TelemetryMessage message) {
        Instant now = Instant.now();

        // 1. Save raw telemetry entity
        TelemetryEventEntity rawEvent = TelemetryEventEntity.builder()
                .id(message.eventId())
                .gameId(message.gameId())
                .playerId(message.playerId())
                .category(message.category())
                .eventType(message.eventType())
                .rawPayload(message.payload())
                .occurredAt(message.timestamp() != null ? message.timestamp() : now)
                .receivedAt(now)
                .build();
        telemetryEventRepository.save(rawEvent);

        // 2. Generate structured digest via Spring AI or intelligent fallback
        CommunityDigest digest = generateCommunityDigest(message);

        // 3. Persist AI-curated community feed item
        CommunityFeedEntity feedEntity = CommunityFeedEntity.builder()
                .id(UUID.randomUUID())
                .telemetryEventId(message.eventId())
                .gameId(message.gameId())
                .authorGamerTag(message.playerId())
                .category(message.category())
                .headline(digest.headline())
                .summary(digest.summary())
                .severity(digest.severity())
                .sentiment(digest.sentiment())
                .performanceImpactScore(digest.performanceImpactScore())
                .recommendedAction(digest.recommendedAction())
                .likeCount(0)
                .commentCount(0)
                .createdAt(now)
                .build();
        communityFeedRepository.save(feedEntity);

        // 4. Persist insight record for unified analytics
        CuratedInsightEntity insightEntity = CuratedInsightEntity.builder()
                .id(UUID.randomUUID())
                .telemetryEventId(message.eventId())
                .sentiment(digest.sentiment())
                .toxicityScore(digest.sentiment() == SentimentScore.TOXIC ? 0.9 : 0.05)
                .summary(digest.summary())
                .flaggedToxicity(digest.sentiment() == SentimentScore.TOXIC || digest.severity() == SeverityLevel.CRITICAL)
                .curatedAt(now)
                .build();
        curatedInsightRepository.save(insightEntity);

        log.info("AgentService -> Curated [{}] for game {} | Severity: {} | Headline: '{}'",
                message.eventId(), message.gameId(), feedEntity.getSeverity(), feedEntity.getHeadline());

        return feedEntity;
    }

    /**
     * Query live AI-curated community feed with optional severity filtering.
     */
    @Transactional(readOnly = true)
    public List<CommunityFeedEntity> getLiveFeed(SeverityLevel severity, int limit) {
        int boundedLimit = Math.clamp(limit, 1, 100);
        Pageable pageable = PageRequest.of(0, boundedLimit);

        if (severity != null) {
            return communityFeedRepository.findBySeverityOrderByCreatedAtDesc(severity, pageable);
        }
        return communityFeedRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<CommunityFeedEntity> getFeedItem(UUID id) {
        return communityFeedRepository.findById(id);
    }

    /**
     * Dispatches prompt to Spring AI ChatClient or triggers local heuristic digest generator.
     */
    private CommunityDigest generateCommunityDigest(TelemetryMessage message) {
        if (!"fallback".equalsIgnoreCase(configuredProvider) && chatClient.isPresent()) {
            try {
                return callSpringAiChatClient(message);
            } catch (Exception e) {
                log.warn("Spring AI invocation failed for event [{}], falling back to local heuristic: {}",
                        message.eventId(), e.getMessage());
            }
        }
        return generateHeuristicDigest(message);
    }

    private CommunityDigest callSpringAiChatClient(TelemetryMessage message) {
        String userPrompt = PromptTemplates.USER_EVENT_TEMPLATE
                .replace("{gameId}", message.gameId())
                .replace("{playerId}", message.playerId())
                .replace("{category}", message.category().name())
                .replace("{eventType}", message.eventType())
                .replace("{payload}", message.payload() != null ? message.payload() : "");

        String rawContent = chatClient.get()
                .prompt()
                .system(PromptTemplates.SYSTEM_COMMUNITY_DIGEST_PROMPT)
                .user(userPrompt)
                .call()
                .content();

        return parseDigestResponse(rawContent, message);
    }

    private CommunityDigest parseDigestResponse(String rawContent, TelemetryMessage message) {
        try {
            String cleanJson = rawContent.replaceAll("^```json\\s*", "").replaceAll("\\s*```$", "").trim();
            JsonNode root = objectMapper.readTree(cleanJson);

            String headline = root.path("headline").asText("Telemetry Update");
            String summary = root.path("summary").asText("Telemetry processed by Spring AI Agent.");
            SeverityLevel severity = SeverityLevel.valueOf(root.path("severity").asText("LOW").toUpperCase());
            SentimentScore sentiment = SentimentScore.valueOf(root.path("sentiment").asText("NEUTRAL").toUpperCase());
            double impact = root.path("performanceImpactScore").asDouble(0.0);
            String action = root.path("recommendedAction").asText("Monitor logs");

            return new CommunityDigest(headline, summary, severity, sentiment, impact, action);
        } catch (Exception e) {
            log.warn("Failed to parse Spring AI JSON payload. Falling back to heuristic: {}", rawContent);
            return generateHeuristicDigest(message);
        }
    }

    /**
     * Local heuristic analyzer designed to detect error logs, performance spikes,
     * and toxic/positive sentiment out of the box without external API keys.
     */
    public CommunityDigest generateHeuristicDigest(TelemetryMessage message) {
        String payload = (message.payload() != null ? message.payload() : "").toLowerCase(Locale.ROOT);
        String eventType = (message.eventType() != null ? message.eventType() : "").toLowerCase(Locale.ROOT);

        // 1. Detect System Crash / Severe Error Logs
        if (payload.contains("crash") || payload.contains("nullpointer") || payload.contains("exception")
                || payload.contains("fatal") || eventType.contains("crash") || payload.contains("disconnect")) {
            return new CommunityDigest(
                    "Critical Error: Game Instance Failure in " + message.gameId(),
                    "Severe runtime error encountered during match: " + truncate(message.payload(), 120),
                    SeverityLevel.CRITICAL,
                    SentimentScore.NEGATIVE,
                    0.95,
                    "Escalate to server reliability engineering; check container logs"
            );
        }

        // 2. Detect Performance Spikes (Lag, Ping, FPS Drops)
        if (payload.contains("ping") || payload.contains("latency") || payload.contains("fps")
                || payload.contains("packetloss") || payload.contains("lag") || payload.contains("stutter")) {
            boolean isHighLag = payload.contains("high") || payload.contains("loss") || payload.contains("spike");
            SeverityLevel level = isHighLag ? SeverityLevel.HIGH : SeverityLevel.MEDIUM;
            double impact = isHighLag ? 0.75 : 0.40;

            return new CommunityDigest(
                    "Performance Alert: Network Latency / FPS Fluctuation",
                    "Telemetry reported network or frame rate anomalies for player " + message.playerId(),
                    level,
                    SentimentScore.NEGATIVE,
                    impact,
                    "Verify regional server network routing and frame pacing"
            );
        }

        // 3. Detect In-Game Toxic Griefing
        if (payload.contains("trash") || payload.contains("feeder") || payload.contains("idiot")
                || payload.contains("uninstall") || payload.contains("cheat") || payload.contains("kys")) {
            return new CommunityDigest(
                    "Moderation Alert: Toxic Behavior Flagged",
                    "Inappropriate player communication intercepted in lobby " + message.gameId(),
                    SeverityLevel.HIGH,
                    SentimentScore.TOXIC,
                    0.10,
                    "Review player chat history and apply automated communication strike"
            );
        }

        // 4. Detect Sportsmanship & Positive Highlights
        if (payload.contains("gg") || payload.contains("wp") || payload.contains("clutch")
                || payload.contains("commend") || payload.contains("great") || payload.contains("win")) {
            return new CommunityDigest(
                    "Match Highlight: Commendable Sportsmanship",
                    "Positive teamwork and clutch engagement recorded for player " + message.playerId(),
                    SeverityLevel.LOW,
                    SentimentScore.POSITIVE,
                    0.0,
                    "Reward player sportsmanship score"
            );
        }

        // 5. Default General Gameplay
        return new CommunityDigest(
                "Telemetry Event: " + message.eventType(),
                "Standard game telemetry logged for player " + message.playerId() + " in " + message.gameId(),
                SeverityLevel.LOW,
                SentimentScore.NEUTRAL,
                0.02,
                "Routine data ingest"
        );
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() <= maxLen ? text : text.substring(0, maxLen) + "...";
    }
}

