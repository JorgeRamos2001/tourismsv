package com.tourismsv.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewResponse(
        UUID id,
        Integer value,
        String content,
        UUID userId,
        String userName,
        LocalDateTime createdAt
) {}
