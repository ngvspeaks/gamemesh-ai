package com.gamemesh.ai.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamemesh.ai.model.dto.TelemetryMessage;
import com.gamemesh.ai.model.entity.CuratedInsightEntity;
import com.gamemesh.ai.model.enums.SentimentScore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Slf4j
@Component
public class TelemetryCurationAgent {

    private final Optional<ChatClient> chatClient;
    private final ObjectMapper objectMapper;

    @Value("${gamemesh.ai.provider:fallback}")
    private String configuredProvider;

    private static final Set<String> TOXIC_PATTERNS = Set.of(
            "trash", "feeder", "idiot", "uninstall", "hack", "cheat", "kys", "hate", "scam"
    );

    private static final Set<String> NEGATIVE_PATTERNS = Set.of(
            "bad", "lost", "fail", "lag", "died", "terrible", "worst", "unlucky"
    );

    private static final Set<String> POSITIVE_PATTERNS = Set.of(
            "gg", "wp", "nice", "great", "good job", "win", "thanks", "clutch", "commend"
    );

    public TelemetryCurationAgent(Optional<ChatClient> chatClient, ObjectMapper objectMapper) {
        this.chatClient = chatClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Autonomously curates the incoming telemetry event with sentiment & toxicity analysis.
     */
    public CuratedInsightEntity analyze(TelemetryMessage message) {
        if (!"fallback".equalsIgnoreCase(configuredProvider) && chatClient.isPresent()) {
            try {
                return analyzeWithSpringAi(message);
            } catch (Exception e) {
                log.warn("Spring AI invocation encountered an error. Falling back to autonomous heuristic agent: {}", e.getMessage());
            }
        }
        return analyzeWithHeuristicAgent(message);
    }

    private CuratedInsightEntity analyzeWithSpringAi(TelemetryMessage message) {
        String userPrompt = PromptTemplates.USER_EVENT_TEMPLATE
                .replace("{gameId}", message.gameId())
                .replace("{playerId}", message.playerId())
                .replace("{category}", message.category().name())
                .replace("{eventType}", message.eventType())
                .replace("{payload}", message.payload());

        log.debug("Invoking Spring AI for event: {}", message.eventId());

        String responseContent = chatClient.get()
                .prompt()
                .system(PromptTemplates.SYSTEM_CURATION_PROMPT)
                .user(userPrompt)
                .call()
                .content();

        return parseAiResponse(message.eventId(), responseContent);
    }

    private CuratedInsightEntity parseAiResponse(UUID eventId, String rawContent) {
        try {
            // Strip any accidental markdown formatting
            String cleanJson = rawContent.replaceAll("^```json\\s*", "").replaceAll("\\s*```$", "").trim();
            JsonNode root = objectMapper.readTree(cleanJson);

            SentimentScore sentiment = SentimentScore.valueOf(root.path("sentiment").asText(SentimentScore.NEUTRAL.name()).toUpperCase());
            double toxicityScore = root.path("toxicityScore").asDouble(0.1);
            String summary = root.path("summary").asText("Telemetry event processed by Spring AI");
            boolean flagged = root.path("flaggedToxicity").asBoolean(toxicityScore >= 0.7);

            return CuratedInsightEntity.builder()
                    .id(UUID.randomUUID())
                    .telemetryEventId(eventId)
                    .sentiment(sentiment)
                    .toxicityScore(toxicityScore)
                    .summary(summary)
                    .flaggedToxicity(flagged)
                    .curatedAt(Instant.now())
                    .build();
        } catch (Exception e) {
            log.error("Failed to parse Spring AI JSON response: {}. Reverting to heuristic fallback.", rawContent, e);
            return fallbackInsight(eventId, "AI response parsing failed, baseline curation assigned", SentimentScore.NEUTRAL, 0.1, false);
        }
    }

    /**
     * Local zero-cost heuristic sentiment & toxicity analyzer.
     */
    private CuratedInsightEntity analyzeWithHeuristicAgent(TelemetryMessage message) {
        String text = (message.payload() != null ? message.payload() : "").toLowerCase(Locale.ROOT);

        SentimentScore score = SentimentScore.NEUTRAL;
        double toxicity = 0.05;
        boolean flagged = false;
        String summary;

        for (String pattern : TOXIC_PATTERNS) {
            if (text.contains(pattern)) {
                score = SentimentScore.TOXIC;
                toxicity = 0.88;
                flagged = true;
                break;
            }
        }

        if (!flagged) {
            for (String pattern : NEGATIVE_PATTERNS) {
                if (text.contains(pattern)) {
                    score = SentimentScore.NEGATIVE;
                    toxicity = 0.45;
                    break;
                }
            }

            for (String pattern : POSITIVE_PATTERNS) {
                if (text.contains(pattern)) {
                    score = SentimentScore.POSITIVE;
                    toxicity = 0.02;
                    break;
                }
            }
        }

        summary = String.format("Telemetry %s event curated for player %s in game %s [Sentiment: %s]",
                message.category(), message.playerId(), message.gameId(), score);

        return CuratedInsightEntity.builder()
                .id(UUID.randomUUID())
                .telemetryEventId(message.eventId())
                .sentiment(score)
                .toxicityScore(toxicity)
                .summary(summary)
                .flaggedToxicity(flagged)
                .curatedAt(Instant.now())
                .build();
    }

    private CuratedInsightEntity fallbackInsight(UUID eventId, String summary, SentimentScore sentiment, double toxicity, boolean flagged) {
        return CuratedInsightEntity.builder()
                .id(UUID.randomUUID())
                .telemetryEventId(eventId)
                .sentiment(sentiment)
                .toxicityScore(toxicity)
                .summary(summary)
                .flaggedToxicity(flagged)
                .curatedAt(Instant.now())
                .build();
    }
}

