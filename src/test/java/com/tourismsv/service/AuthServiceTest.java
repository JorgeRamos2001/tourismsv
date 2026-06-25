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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthService authService;

    private static final String ACCESS_TOKEN = "access-token";
    private static final String REFRESH_TOKEN = "refresh-token";
    private static final long ACCESS_EXPIRATION = 900000L;
    private static final long REFRESH_EXPIRATION_MS = 604800000L;

    private static final User TEST_USER = User.builder()
            .id(UUID.randomUUID())
            .name("Test User")
            .email("test@example.com")
            .password("encoded-pass")
            .role(Role.TOURIST)
            .state(UserState.ACTIVE)
            .build();

    private static final String RAW_PASSWORD = "password123";

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, refreshTokenRepository, jwtService, passwordEncoder);
        ReflectionTestUtils.setField(authService, "refreshExpirationMs", REFRESH_EXPIRATION_MS);
    }

    @Test
    void register_whenEmailAlreadyExists_shouldThrow() {
        var request = new RegisterRequest("Test User", "test@example.com", RAW_PASSWORD);

        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        var ex = assertThrows(ConflictException.class, () -> authService.register(request));
        assertEquals("Email already registered: " + request.email(), ex.getMessage());

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_whenSuccess_shouldReturnAuthResponse() {
        var request = new RegisterRequest("Test User", "test@example.com", RAW_PASSWORD);
        var savedUser = User.builder()
                .id(UUID.randomUUID())
                .name(request.name())
                .email(request.email())
                .password("encoded-pass")
                .role(Role.TOURIST)
                .state(UserState.ACTIVE)
                .build();

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-pass");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateAccessToken(savedUser.getEmail())).thenReturn(ACCESS_TOKEN);
        when(jwtService.generateRefreshToken(savedUser.getEmail())).thenReturn(REFRESH_TOKEN);
        when(jwtService.getAccessExpirationMs()).thenReturn(ACCESS_EXPIRATION);

        var response = authService.register(request);

        assertNotNull(response);
        assertEquals(ACCESS_TOKEN, response.accessToken());
        assertEquals(REFRESH_TOKEN, response.refreshToken());
        assertEquals(ACCESS_EXPIRATION, response.expiresIn());

        verify(userRepository).existsByEmail(request.email());
        verify(passwordEncoder).encode(request.password());
        verify(userRepository).save(any(User.class));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void login_whenEmailNotFound_shouldThrow() {
        var request = new LoginRequest("unknown@example.com", RAW_PASSWORD);

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

        var ex = assertThrows(BadCredentialsException.class, () -> authService.login(request));
        assertEquals("Invalid email or password", ex.getMessage());
    }

    @Test
    void login_whenWrongPassword_shouldThrow() {
        var request = new LoginRequest("test@example.com", "wrong-password");
        var user = TEST_USER;

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(false);

        var ex = assertThrows(BadCredentialsException.class, () -> authService.login(request));
        assertEquals("Invalid email or password", ex.getMessage());
    }

    @Test
    void login_whenInactiveAccount_shouldThrow() {
        var request = new LoginRequest("test@example.com", RAW_PASSWORD);
        var inactiveUser = User.builder()
                .id(UUID.randomUUID())
                .name("Test User")
                .email("test@example.com")
                .password("encoded-pass")
                .role(Role.TOURIST)
                .state(UserState.INACTIVE)
                .build();

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(inactiveUser));
        when(passwordEncoder.matches(request.password(), inactiveUser.getPassword())).thenReturn(true);

        var ex = assertThrows(BusinessException.class, () -> authService.login(request));
        assertEquals("Account is not active", ex.getMessage());
    }

    @Test
    void login_whenSuccess_shouldReturnAuthResponse() {
        var request = new LoginRequest("test@example.com", RAW_PASSWORD);
        var user = TEST_USER;

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(true);
        when(jwtService.generateAccessToken(user.getEmail())).thenReturn(ACCESS_TOKEN);
        when(jwtService.generateRefreshToken(user.getEmail())).thenReturn(REFRESH_TOKEN);
        when(jwtService.getAccessExpirationMs()).thenReturn(ACCESS_EXPIRATION);

        var response = authService.login(request);

        assertNotNull(response);
        assertEquals(ACCESS_TOKEN, response.accessToken());
        assertEquals(REFRESH_TOKEN, response.refreshToken());
        assertEquals(ACCESS_EXPIRATION, response.expiresIn());

        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void refresh_whenTokenNotFound_shouldThrow() {
        var request = new RefreshTokenRequest("invalid-token");

        when(refreshTokenRepository.findByToken(request.refreshToken())).thenReturn(Optional.empty());

        var ex = assertThrows(BusinessException.class, () -> authService.refresh(request));
        assertEquals("Invalid refresh token", ex.getMessage());
    }

    @Test
    void refresh_whenTokenExpired_shouldThrow() {
        var request = new RefreshTokenRequest("expired-token");
        var expiredToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .token("expired-token")
                .user(TEST_USER)
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .build();

        when(refreshTokenRepository.findByToken(request.refreshToken())).thenReturn(Optional.of(expiredToken));

        var ex = assertThrows(BusinessException.class, () -> authService.refresh(request));
        assertEquals("Refresh token has expired", ex.getMessage());

        verify(refreshTokenRepository).delete(expiredToken);
    }

    @Test
    void refresh_whenSuccess_shouldReturnNewAccessToken() {
        var request = new RefreshTokenRequest("valid-token");
        var validToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .token("valid-token")
                .user(TEST_USER)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();

        when(refreshTokenRepository.findByToken(request.refreshToken())).thenReturn(Optional.of(validToken));
        when(jwtService.generateAccessToken(TEST_USER.getEmail())).thenReturn(ACCESS_TOKEN);
        when(jwtService.getAccessExpirationMs()).thenReturn(ACCESS_EXPIRATION);

        var response = authService.refresh(request);

        assertNotNull(response);
        assertEquals(ACCESS_TOKEN, response.accessToken());
        assertEquals("valid-token", response.refreshToken());
        assertEquals(ACCESS_EXPIRATION, response.expiresIn());
    }

    @Test
    void logout_whenTokenFound_shouldDelete() {
        var token = "existing-token";
        var refreshToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .token(token)
                .user(TEST_USER)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();

        when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.of(refreshToken));

        authService.logout(token);

        verify(refreshTokenRepository).delete(refreshToken);
    }

    @Test
    void logout_whenTokenNotFound_shouldDoNothing() {
        var token = "non-existent-token";

        when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.empty());

        authService.logout(token);

        verify(refreshTokenRepository, never()).delete(any());
    }
}
