package com.tourismsv.exception;

import java.time.LocalDateTime;

public record ExceptionResponse(
        Integer status,
        String error,
        Object details,
        String path,
        LocalDateTime timestamp
) {}
