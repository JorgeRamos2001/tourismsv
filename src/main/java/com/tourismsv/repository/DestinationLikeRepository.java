package com.tourismsv.repository;

import com.tourismsv.domain.entity.DestinationLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DestinationLikeRepository extends JpaRepository<DestinationLike, UUID> {

    boolean existsByDestinationIdAndUserId(UUID destinationId, UUID userId);

    Optional<DestinationLike> findByDestinationIdAndUserId(UUID destinationId, UUID userId);

    long countByDestinationId(UUID destinationId);

    void deleteByDestinationIdAndUserId(UUID destinationId, UUID userId);
}
