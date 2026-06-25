package com.tourismsv.repository;

import com.tourismsv.domain.entity.Destination;
import com.tourismsv.domain.enums.DestinationState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface DestinationRepository extends JpaRepository<Destination, UUID> {

    Page<Destination> findByState(DestinationState state, Pageable pageable);

    Page<Destination> findByDestinationTypeId(UUID destinationTypeId, Pageable pageable);

    @Query(value = """
            SELECT * FROM destinations d
            WHERE (:name IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%')))
              AND (:state IS NULL OR d.state::text = :state)
              AND (:destinationTypeId IS NULL OR d.destination_type_id = :destinationTypeId)
              AND (:country IS NULL OR LOWER(d.country) LIKE LOWER(CONCAT('%', :country, '%')))
              AND (:city IS NULL OR LOWER(d.city) LIKE LOWER(CONCAT('%', :city, '%')))
            """,
           countQuery = """
            SELECT COUNT(*) FROM destinations d
            WHERE (:name IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%')))
              AND (:state IS NULL OR d.state::text = :state)
              AND (:destinationTypeId IS NULL OR d.destination_type_id = :destinationTypeId)
              AND (:country IS NULL OR LOWER(d.country) LIKE LOWER(CONCAT('%', :country, '%')))
              AND (:city IS NULL OR LOWER(d.city) LIKE LOWER(CONCAT('%', :city, '%')))
            """,
           nativeQuery = true)
    Page<Destination> search(@Param("name") String name,
                             @Param("state") String state,
                             @Param("destinationTypeId") UUID destinationTypeId,
                             @Param("country") String country,
                             @Param("city") String city,
                             Pageable pageable);

    boolean existsByName(String name);
}
