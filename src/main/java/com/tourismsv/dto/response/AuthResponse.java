package com.tourismsv.dto.response;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        long expiresIn
) {}
