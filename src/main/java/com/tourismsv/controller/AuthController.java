package com.tourismsv.controller;

import com.tourismsv.dto.request.LoginRequest;
import com.tourismsv.dto.request.RefreshTokenRequest;
import com.tourismsv.dto.request.RegisterRequest;
import com.tourismsv.dto.response.AuthResponse;
import com.tourismsv.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @GetMapping("/oauth2/callback")
    public ResponseEntity<AuthResponse> oauth2Callback(
            @RequestParam String accessToken,
            @RequestParam String refreshToken,
            @RequestParam long expiresIn) {
        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken, expiresIn));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request,
                                        Authentication authentication) {
        authService.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }
}
