package com.gamemesh.ai.repository;

import com.gamemesh.ai.model.entity.CuratedInsightEntity;
import com.gamemesh.ai.model.enums.SentimentScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CuratedInsightRepository extends JpaRepository<CuratedInsightEntity, UUID> {

    Optional<CuratedInsightEntity> findByTelemetryEventId(UUID telemetryEventId);

    List<CuratedInsightEntity> findByFlaggedToxicityTrueOrderByCuratedAtDesc();

    List<CuratedInsightEntity> findBySentimentOrderByCuratedAtDesc(SentimentScore sentiment);

    long countBySentiment(SentimentScore sentiment);

    long countByFlaggedToxicityTrue();
}

