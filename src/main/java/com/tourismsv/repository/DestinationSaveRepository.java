package com.tourismsv.repository;

import com.tourismsv.domain.entity.DestinationSave;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DestinationSaveRepository extends JpaRepository<DestinationSave, UUID> {

    Page<DestinationSave> findByUserId(UUID userId, Pageable pageable);

    boolean existsByDestinationIdAndUserId(UUID destinationId, UUID userId);

    Optional<DestinationSave> findByDestinationIdAndUserId(UUID destinationId, UUID userId);

    long countByDestinationId(UUID destinationId);

    void deleteByDestinationIdAndUserId(UUID destinationId, UUID userId);
}
