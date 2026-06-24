package com.tourismsv.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReviewRequest(
        @NotNull @Min(1) @Max(5) Integer value,
        @NotNull String content
) {}
