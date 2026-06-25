package com.tourismsv.controller;

import tools.jackson.databind.ObjectMapper;
import com.tourismsv.domain.entity.User;
import com.tourismsv.domain.enums.Role;
import com.tourismsv.domain.enums.UserState;
import com.tourismsv.dto.request.ReviewRequest;
import com.tourismsv.dto.response.ReviewResponse;
import com.tourismsv.repository.UserRepository;
import com.tourismsv.security.JwtService;
import com.tourismsv.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewController.class)
@Import(TestSecurityConfig.class)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReviewService reviewService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private UserRepository userRepository;

    private static final User TEST_USER = User.builder()
            .id(UUID.randomUUID())
            .name("Test")
            .email("user")
            .password("pass")
            .role(Role.TOURIST)
            .state(UserState.ACTIVE)
            .build();

    @BeforeEach
    void setUp() {
        lenient().when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(TEST_USER));
    }

    private ReviewResponse sampleReview() {
        return new ReviewResponse(UUID.randomUUID(), 5, "Great place!", UUID.randomUUID(), "User", LocalDateTime.now());
    }

    @Test
    void findByDestination_shouldReturn200AndPage() throws Exception {
        var destId = UUID.randomUUID();
        var page = new PageImpl<>(List.of(sampleReview()));

        when(reviewService.findByDestinationId(eq(destId), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/destinations/{destinationId}/reviews", destId)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].value").value(5));
    }

    @Test
    @WithMockUser
    void create_shouldReturn201() throws Exception {
        var destId = UUID.randomUUID();
        var request = new ReviewRequest(4, "Nice place!");
        var response = sampleReview();

        when(reviewService.create(eq(destId), any(), any(ReviewRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/destinations/{destinationId}/reviews", destId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.value").value(5));
    }

    @Test
    void create_shouldReturn401WhenAnonymous() throws Exception {
        var destId = UUID.randomUUID();
        var request = new ReviewRequest(4, "Nice place!");

        mockMvc.perform(post("/api/v1/destinations/{destinationId}/reviews", destId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void create_shouldReturn400WhenValueOutOfRange() throws Exception {
        var destId = UUID.randomUUID();
        var request = new ReviewRequest(6, "Nice place!");

        mockMvc.perform(post("/api/v1/destinations/{destinationId}/reviews", destId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    @WithMockUser
    void update_shouldReturn200() throws Exception {
        var destId = UUID.randomUUID();
        var reviewId = UUID.randomUUID();
        var request = new ReviewRequest(3, "Updated review");
        var response = new ReviewResponse(reviewId, 3, "Updated review", UUID.randomUUID(), "User", LocalDateTime.now());

        when(reviewService.update(eq(destId), eq(reviewId), any(), any(ReviewRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/destinations/{destinationId}/reviews/{reviewId}", destId, reviewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").value(3));
    }

    @Test
    @WithMockUser
    void delete_shouldReturn204() throws Exception {
        var destId = UUID.randomUUID();
        var reviewId = UUID.randomUUID();

        doNothing().when(reviewService).delete(eq(destId), eq(reviewId), any());

        mockMvc.perform(delete("/api/v1/destinations/{destinationId}/reviews/{reviewId}", destId, reviewId))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_shouldReturn401WhenAnonymous() throws Exception {
        var destId = UUID.randomUUID();
        var reviewId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/destinations/{destinationId}/reviews/{reviewId}", destId, reviewId))
                .andExpect(status().isUnauthorized());
    }
}
