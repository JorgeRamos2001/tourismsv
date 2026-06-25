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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class AuthServiceIntegrationTest {

    @LocalServerPort
    private int port;

    private RestClient restClient;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void setUp() {
        restClient = RestClient.create("http://localhost:" + port);
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void register_login_refresh_logout() {
        var registerReq = new RegisterRequest("Test User", "test@example.com", "password123");
        var registerRes = restClient.post().uri("/api/v1/auth/register").body(registerReq).retrieve().toEntity(AuthResponse.class);

        assertThat(registerRes.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(201));
        assertThat(registerRes.getBody()).isNotNull();
        var registerBody = registerRes.getBody();
        assertThat(registerBody.accessToken()).isNotBlank();
        assertThat(registerBody.refreshToken()).isNotBlank();
        assertThat(registerBody.expiresIn()).isPositive();

        var loginReq = new LoginRequest("test@example.com", "password123");
        var loginRes = restClient.post().uri("/api/v1/auth/login").body(loginReq).retrieve().toEntity(AuthResponse.class);

        assertThat(loginRes.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(loginRes.getBody()).isNotNull();
        var loginBody = loginRes.getBody();
        assertThat(loginBody.accessToken()).isNotBlank();
        assertThat(loginBody.refreshToken()).isNotBlank();

        var refreshReq = new RefreshTokenRequest(loginBody.refreshToken());
        var refreshRes = restClient.post().uri("/api/v1/auth/refresh").body(refreshReq).retrieve().toEntity(AuthResponse.class);

        assertThat(refreshRes.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(refreshRes.getBody()).isNotNull();
        var refreshBody = refreshRes.getBody();
        assertThat(refreshBody.accessToken()).isNotBlank();
        assertThat(refreshBody.refreshToken()).isEqualTo(loginBody.refreshToken());

        var logoutRes = restClient.post().uri("/api/v1/auth/logout")
                .body(new RefreshTokenRequest(refreshBody.refreshToken()))
                .header("Authorization", "Bearer " + refreshBody.accessToken())
                .retrieve().toBodilessEntity();

        assertThat(logoutRes.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(204));
        assertThat(refreshTokenRepository.findByToken(refreshBody.refreshToken())).isEmpty();
    }

    @Test
    void register_shouldReturn409WhenEmailExists() {
        var request = new RegisterRequest("Test User", "duplicate@example.com", "password123");
        restClient.post().uri("/api/v1/auth/register").body(request).retrieve().toEntity(AuthResponse.class);

        org.springframework.web.client.HttpClientErrorException exception = org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.web.client.HttpClientErrorException.class,
                () -> restClient.post().uri("/api/v1/auth/register").body(request).retrieve().toEntity(AuthResponse.class)
        );

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(409));
    }

    @Test
    void login_shouldReturn401WhenInvalidCredentials() {
        var request = new RegisterRequest("Test User", "logintest@example.com", "password123");
        restClient.post().uri("/api/v1/auth/register").body(request).retrieve().toEntity(AuthResponse.class);

        var badLogin = new LoginRequest("logintest@example.com", "wrongpassword");
        org.springframework.web.client.HttpClientErrorException exception = org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.web.client.HttpClientErrorException.class,
                () -> restClient.post().uri("/api/v1/auth/login").body(badLogin).retrieve().toEntity(AuthResponse.class)
        );

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(401));
    }

    @Test
    void register_shouldReturn400WhenValidationFails() {
        var request = new RegisterRequest("", "invalid", "pw");
        org.springframework.web.client.HttpClientErrorException exception = org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.web.client.HttpClientErrorException.class,
                () -> restClient.post().uri("/api/v1/auth/register").body(request).retrieve().toEntity(AuthResponse.class)
        );

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(400));
    }
}
