package com.tourismsv.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import tools.jackson.databind.ObjectMapper;

@TestConfiguration
@EnableWebSecurity
@EnableMethodSecurity
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/v1/auth/**").permitAll()
                    .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/destination-types/**").permitAll()
                    .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/destinations").permitAll()
                    .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/destinations/*").permitAll()
                    .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/destinations/*/images").permitAll()
                    .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/destinations/*/reviews").permitAll()
                    .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/destinations/*/likes").permitAll()
                    .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/destinations/*/saves").permitAll()
                    .anyRequest().authenticated())
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint((request, response, authException) ->
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                    .accessDeniedHandler((request, response, accessDeniedException) ->
                            response.sendError(HttpServletResponse.SC_FORBIDDEN)));
        return http.build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
