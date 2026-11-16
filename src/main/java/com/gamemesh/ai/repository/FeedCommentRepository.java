package com.gamemesh.ai.repository;

import com.gamemesh.ai.model.entity.FeedCommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FeedCommentRepository extends JpaRepository<FeedCommentEntity, UUID> {

    List<FeedCommentEntity> findByFeedIdOrderByCreatedAtAsc(UUID feedId);

    long countByFeedId(UUID feedId);
}

