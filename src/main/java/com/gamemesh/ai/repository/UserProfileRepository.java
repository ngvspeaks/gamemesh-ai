package com.gamemesh.ai.repository;

import com.gamemesh.ai.model.entity.UserProfileEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfileEntity, UUID> {

    Optional<UserProfileEntity> findByGamerTagIgnoreCase(String gamerTag);

    boolean existsByGamerTagIgnoreCase(String gamerTag);

    List<UserProfileEntity> findAllByOrderByReputationScoreDesc(Pageable pageable);
}

