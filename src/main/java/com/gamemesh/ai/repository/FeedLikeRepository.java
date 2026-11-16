package com.gamemesh.ai.repository;

import com.gamemesh.ai.model.entity.FeedLikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FeedLikeRepository extends JpaRepository<FeedLikeEntity, UUID> {

    Optional<FeedLikeEntity> findByFeedIdAndGamerTagIgnoreCase(UUID feedId, String gamerTag);

    boolean existsByFeedIdAndGamerTagIgnoreCase(UUID feedId, String gamerTag);

    long countByFeedId(UUID feedId);

    void deleteByFeedIdAndGamerTagIgnoreCase(UUID feedId, String gamerTag);
}

