package com.tourismsv.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record DestinationRequest(
        @NotBlank String name,
        @NotBlank String description,
        @NotBlank String country,
        @NotBlank String city,
        @NotNull BigDecimal latitude,
        @NotNull BigDecimal longitude,
        @NotNull UUID destinationTypeId,
        String urlBanner
) {}
