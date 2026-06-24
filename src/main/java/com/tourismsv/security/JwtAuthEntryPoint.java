package com.tourismsv.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourismsv.exception.ExceptionResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        var errorResponse = new ExceptionResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "UNAUTHORIZED",
                authException.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now()
        );

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
