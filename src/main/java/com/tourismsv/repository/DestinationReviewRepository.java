package com.tourismsv.repository;

import com.tourismsv.domain.entity.DestinationReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DestinationReviewRepository extends JpaRepository<DestinationReview, UUID> {

    Page<DestinationReview> findByDestinationId(UUID destinationId, Pageable pageable);

    Optional<DestinationReview> findByDestinationIdAndUserId(UUID destinationId, UUID userId);

    boolean existsByDestinationIdAndUserId(UUID destinationId, UUID userId);
}
