package com.gamemesh.ai.repository;

import com.gamemesh.ai.model.entity.TelemetryEventEntity;
import com.gamemesh.ai.model.enums.TelemetryCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TelemetryEventRepository extends JpaRepository<TelemetryEventEntity, UUID> {

    List<TelemetryEventEntity> findByGameIdOrderByReceivedAtDesc(String gameId);

    List<TelemetryEventEntity> findByPlayerIdOrderByReceivedAtDesc(String playerId);

    List<TelemetryEventEntity> findByCategoryOrderByReceivedAtDesc(TelemetryCategory category);

    long countByGameId(String gameId);
}

