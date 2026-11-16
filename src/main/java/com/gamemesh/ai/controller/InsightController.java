package com.gamemesh.ai.controller;

import com.gamemesh.ai.model.dto.CuratedAnalysisResponse;
import com.gamemesh.ai.service.InsightQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/insights")
public class InsightController {

    private final InsightQueryService queryService;

    public InsightController(InsightQueryService queryService) {
        this.queryService = queryService;
    }

    /**
     * Fetch AI curation and sentiment analysis for an event.
     */
    @GetMapping("/events/{eventId}")
    public ResponseEntity<CuratedAnalysisResponse> getEventInsight(@PathVariable UUID eventId) {
        return queryService.getInsightForEvent(eventId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retrieve all events flagged for toxicity or harassment.
     */
    @GetMapping("/flagged")
    public ResponseEntity<List<CuratedAnalysisResponse>> getFlaggedIncidents() {
        return ResponseEntity.ok(queryService.getFlaggedIncidents());
    }

    /**
     * Aggregate analytics showing sentiment distribution and total events processed.
     */
    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getAnalytics() {
        return ResponseEntity.ok(queryService.getTelemetryAnalytics());
    }
}

