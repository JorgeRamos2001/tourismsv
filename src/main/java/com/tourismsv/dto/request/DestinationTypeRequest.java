package com.tourismsv.dto.request;

import jakarta.validation.constraints.NotBlank;

public record DestinationTypeRequest(
        @NotBlank String name,
        @NotBlank String description
) {}
