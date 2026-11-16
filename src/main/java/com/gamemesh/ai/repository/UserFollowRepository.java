package com.gamemesh.ai.repository;

import com.gamemesh.ai.model.entity.UserFollowEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserFollowRepository extends JpaRepository<UserFollowEntity, UUID> {

    Optional<UserFollowEntity> findByFollowerTagIgnoreCaseAndFollowingTagIgnoreCase(String followerTag, String followingTag);

    boolean existsByFollowerTagIgnoreCaseAndFollowingTagIgnoreCase(String followerTag, String followingTag);

    List<UserFollowEntity> findByFollowerTagIgnoreCase(String followerTag);

    List<UserFollowEntity> findByFollowingTagIgnoreCase(String followingTag);

    long countByFollowerTagIgnoreCase(String followerTag);

    long countByFollowingTagIgnoreCase(String followingTag);
}

