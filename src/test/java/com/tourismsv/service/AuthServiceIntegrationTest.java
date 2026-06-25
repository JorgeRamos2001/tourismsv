package com.tourismsv.service;

import com.tourismsv.config.TestcontainersConfiguration;
import com.tourismsv.dto.request.LoginRequest;
import com.tourismsv.dto.request.RefreshTokenRequest;
import com.tourismsv.dto.request.RegisterRequest;
import com.tourismsv.dto.response.AuthResponse;
import com.tourismsv.repository.RefreshTokenRepository;
import com.tourismsv.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class AuthServiceIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void cleanUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void register_login_refresh_logout() {
        // Register
        var registerReq = new RegisterRequest("Test User", "test@example.com", "password123");
        ResponseEntity<AuthResponse> registerRes = restTemplate.postForEntity(
                "/api/v1/auth/register", registerReq, AuthResponse.class);

        assertThat(registerRes.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(registerRes.getBody()).isNotNull();
        var registerBody = registerRes.getBody();
        assertThat(registerBody.accessToken()).isNotBlank();
        assertThat(registerBody.refreshToken()).isNotBlank();
        assertThat(registerBody.expiresIn()).isPositive();

        // Login
        var loginReq = new LoginRequest("test@example.com", "password123");
        ResponseEntity<AuthResponse> loginRes = restTemplate.postForEntity(
                "/api/v1/auth/login", loginReq, AuthResponse.class);

        assertThat(loginRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginRes.getBody()).isNotNull();
        var loginBody = loginRes.getBody();
        assertThat(loginBody.accessToken()).isNotBlank();
        assertThat(loginBody.refreshToken()).isNotBlank();

        // Refresh
        var refreshReq = new RefreshTokenRequest(loginBody.refreshToken());
        ResponseEntity<AuthResponse> refreshRes = restTemplate.postForEntity(
                "/api/v1/auth/refresh", refreshReq, AuthResponse.class);

        assertThat(refreshRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(refreshRes.getBody()).isNotNull();
        var refreshBody = refreshRes.getBody();
        assertThat(refreshBody.accessToken()).isNotBlank();
        assertThat(refreshBody.refreshToken()).isEqualTo(loginBody.refreshToken());

        // Logout
        var headers = new HttpHeaders();
        headers.setBearerAuth(refreshBody.refreshToken());
        var logoutEntity = new HttpEntity<>(headers);
        ResponseEntity<Void> logoutRes = restTemplate.postForEntity(
                "/api/v1/auth/logout", logoutEntity, Void.class);

        assertThat(logoutRes.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(refreshTokenRepository.findByToken(refreshBody.refreshToken())).isEmpty();
    }

    @Test
    void register_shouldReturn409WhenEmailExists() {
        var request = new RegisterRequest("Test User", "duplicate@example.com", "password123");
        restTemplate.postForEntity("/api/v1/auth/register", request, AuthResponse.class);

        var response = restTemplate.postForEntity(
                "/api/v1/auth/register", request, AuthResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void login_shouldReturn401WhenInvalidCredentials() {
        var request = new RegisterRequest("Test User", "logintest@example.com", "password123");
        restTemplate.postForEntity("/api/v1/auth/register", request, AuthResponse.class);

        var badLogin = new LoginRequest("logintest@example.com", "wrongpassword");
        var response = restTemplate.postForEntity("/api/v1/auth/login", badLogin, AuthResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void register_shouldReturn400WhenValidationFails() {
        var request = new RegisterRequest("", "invalid", "pw");
        var response = restTemplate.postForEntity("/api/v1/auth/register", request, AuthResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
