package com.tourismsv.config;

import com.tourismsv.security.JwtAccessDeniedHandler;
import com.tourismsv.security.JwtAuthEntryPoint;
import com.tourismsv.security.JwtAuthenticationFilter;
import com.tourismsv.security.OAuth2AuthenticationFailureHandler;
import com.tourismsv.security.OAuth2AuthenticationSuccessHandler;
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
    private final PasswordEncoder passwordEncoder;
    private final OAuth2AuthenticationSuccessHandler oAuth2SuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2FailureHandler;

    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/v1/auth/**",
            "/swagger-ui/**",
            "/api-docs/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint(jwtAuthEntryPoint)
                    .accessDeniedHandler(jwtAccessDeniedHandler))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/destination-types/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/destinations/**").permitAll()
                    .anyRequest().authenticated())
            .oauth2Login(oauth2 -> oauth2
                    .successHandler(oAuth2SuccessHandler)
                    .failureHandler(oAuth2FailureHandler))
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        var provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }
}
