package com.tourismsv.service;

import com.tourismsv.domain.entity.RefreshToken;
import com.tourismsv.domain.entity.User;
import com.tourismsv.domain.enums.Role;
import com.tourismsv.domain.enums.UserState;
import com.tourismsv.dto.request.LoginRequest;
import com.tourismsv.dto.request.RefreshTokenRequest;
import com.tourismsv.dto.request.RegisterRequest;
import com.tourismsv.dto.response.AuthResponse;
import com.tourismsv.exception.BusinessException;
import com.tourismsv.exception.ConflictException;
import com.tourismsv.repository.RefreshTokenRepository;
import com.tourismsv.repository.UserRepository;
import com.tourismsv.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already registered: " + request.email());
        }

        var user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.TOURIST)
                .state(UserState.ACTIVE)
                .build();

        user = userRepository.save(user);

        return generateAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        var user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        if (!user.isEnabled()) {
            throw new BusinessException("Account is not active");
        }

        return generateAuthResponse(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        var refreshToken = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new BusinessException("Invalid refresh token"));

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new BusinessException("Refresh token has expired");
        }

        var user = refreshToken.getUser();
        var accessToken = jwtService.generateAccessToken(user.getEmail());

        return new AuthResponse(accessToken, refreshToken.getToken(), jwtService.getAccessExpirationMs());
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(refreshTokenRepository::delete);
    }

    private AuthResponse generateAuthResponse(User user) {
        var accessToken = jwtService.generateAccessToken(user.getEmail());
        var refreshToken = jwtService.generateRefreshToken(user.getEmail());

        var refreshEntity = RefreshToken.builder()
                .token(refreshToken)
                .user(user)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshExpirationMs / 1000))
                .build();

        refreshTokenRepository.save(refreshEntity);

        return new AuthResponse(accessToken, refreshToken, jwtService.getAccessExpirationMs());
    }
}
