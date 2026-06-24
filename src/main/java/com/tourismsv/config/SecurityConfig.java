package com.tourismsv.config;

import com.tourismsv.security.JwtAccessDeniedHandler;
import com.tourismsv.security.JwtAuthEntryPoint;
import com.tourismsv.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final JwtAuthEntryPoint jwtAuthEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final UserDetailsService userDetailsService;

    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/v1/auth/**",
            "/swagger-ui/**",
            "/api-docs/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint(jwtAuthEntryPoint)
                    .accessDeniedHandler(jwtAccessDeniedHandler))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/destination-types/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/destinations/**").permitAll()
                    .anyRequest().authenticated())
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        var provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
