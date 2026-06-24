package com.tourismsv.dto.response;

import com.tourismsv.domain.enums.DestinationState;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record DestinationResponse(
        UUID id,
        String name,
        String description,
        String country,
        String city,
        BigDecimal latitude,
        BigDecimal longitude,
        String destinationTypeName,
        String urlBanner,
        DestinationState state,
        LocalDateTime createdAt,
        long likeCount,
        long saveCount,
        double avgRating
) {}
