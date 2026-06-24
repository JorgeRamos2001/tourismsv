package com.tourismsv.dto.response;

public record LikeResponse(
        boolean liked,
        long likesCount
) {}
