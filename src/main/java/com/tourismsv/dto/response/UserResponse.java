package com.tourismsv.dto.response;

import com.tourismsv.domain.enums.Role;
import com.tourismsv.domain.enums.UserState;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        String urlAvatar,
        Role role,
        UserState state,
        LocalDateTime createdAt
) {}
