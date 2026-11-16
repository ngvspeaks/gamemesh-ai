package com.gamemesh.ai.service;

import com.gamemesh.ai.model.dto.CuratedAnalysisResponse;
import com.gamemesh.ai.model.entity.CuratedInsightEntity;
import com.gamemesh.ai.model.entity.TelemetryEventEntity;
import com.gamemesh.ai.model.enums.SentimentScore;
import com.gamemesh.ai.repository.CuratedInsightRepository;
import com.gamemesh.ai.repository.TelemetryEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(readOnly = true)
public class InsightQueryService {

    private final TelemetryEventRepository telemetryEventRepository;
    private final CuratedInsightRepository curatedInsightRepository;

    public InsightQueryService(
            TelemetryEventRepository telemetryEventRepository,
            CuratedInsightRepository curatedInsightRepository
    ) {
        this.telemetryEventRepository = telemetryEventRepository;
        this.curatedInsightRepository = curatedInsightRepository;
    }

    public List<TelemetryEventEntity> getEventsForGame(String gameId) {
        return telemetryEventRepository.findByGameIdOrderByReceivedAtDesc(gameId);
    }

    public List<CuratedAnalysisResponse> getFlaggedIncidents() {
        return curatedInsightRepository.findByFlaggedToxicityTrueOrderByCuratedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public Optional<CuratedAnalysisResponse> getInsightForEvent(UUID eventId) {
        return curatedInsightRepository.findByTelemetryEventId(eventId)
                .map(this::toResponse);
    }

    public Map<String, Object> getTelemetryAnalytics() {
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("totalTelemetryEvents", telemetryEventRepository.count());
        metrics.put("totalFlaggedToxicIncidents", curatedInsightRepository.countByFlaggedToxicityTrue());

        Map<String, Long> sentimentBreakdown = new LinkedHashMap<>();
        for (SentimentScore score : SentimentScore.values()) {
            sentimentBreakdown.put(score.name(), curatedInsightRepository.countBySentiment(score));
        }
        metrics.put("sentimentBreakdown", sentimentBreakdown);

        return metrics;
    }

    private CuratedAnalysisResponse toResponse(CuratedInsightEntity entity) {
        return new CuratedAnalysisResponse(
                entity.getId(),
                entity.getTelemetryEventId(),
                entity.getSentiment(),
                entity.getToxicityScore(),
                entity.getSummary(),
                entity.isFlaggedToxicity(),
                entity.getCuratedAt()
        );
    }
}

