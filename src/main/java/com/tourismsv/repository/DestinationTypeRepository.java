package com.tourismsv.repository;

import com.tourismsv.domain.entity.DestinationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DestinationTypeRepository extends JpaRepository<DestinationType, UUID> {
}
