package com.tourismsv.dto.request;

import com.tourismsv.domain.enums.UserState;
import jakarta.validation.constraints.NotNull;

public record UserStateUpdateRequest(
        @NotNull UserState state
) {}
