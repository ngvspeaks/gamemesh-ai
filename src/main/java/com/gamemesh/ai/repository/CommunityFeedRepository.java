package com.gamemesh.ai.repository;

import com.gamemesh.ai.model.entity.CommunityFeedEntity;
import com.gamemesh.ai.model.enums.SeverityLevel;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommunityFeedRepository extends JpaRepository<CommunityFeedEntity, UUID> {

    List<CommunityFeedEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<CommunityFeedEntity> findBySeverityOrderByCreatedAtDesc(SeverityLevel severity, Pageable pageable);

    List<CommunityFeedEntity> findByGameIdOrderByCreatedAtDesc(String gameId);

    long countBySeverity(SeverityLevel severity);
}

