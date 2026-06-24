package com.tourismsv.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record DestinationTypeResponse(
        UUID id,
        String name,
        String description,
        LocalDateTime createdAt
) {}
