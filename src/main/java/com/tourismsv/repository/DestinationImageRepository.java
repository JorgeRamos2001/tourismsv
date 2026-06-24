package com.tourismsv.repository;

import com.tourismsv.domain.entity.DestinationImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DestinationImageRepository extends JpaRepository<DestinationImage, UUID> {

    List<DestinationImage> findByDestinationId(UUID destinationId);

    void deleteByDestinationId(UUID destinationId);
}
