package com.tourismsv.dto.response;

public record SaveResponse(
        boolean saved,
        long savesCount
) {}
