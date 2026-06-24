package com.tourismsv.dto.response;

import java.util.UUID;

public record DestinationImageResponse(
        UUID id,
        String urlImage
) {}
