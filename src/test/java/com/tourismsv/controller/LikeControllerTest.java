package com.tourismsv.controller;

import tools.jackson.databind.ObjectMapper;
import com.tourismsv.domain.entity.User;
import com.tourismsv.domain.enums.Role;
import com.tourismsv.domain.enums.UserState;
import com.tourismsv.dto.response.LikeResponse;
import com.tourismsv.repository.UserRepository;
import com.tourismsv.security.JwtService;
import com.tourismsv.service.LikeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LikeController.class)
@Import(TestSecurityConfig.class)
class LikeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LikeService likeService;

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

    @Test
    void getStatus_shouldReturn200WhenAuthenticated() throws Exception {
        var destId = UUID.randomUUID();

        when(likeService.getStatus(eq(destId), any())).thenReturn(new LikeResponse(true, 10L));

        mockMvc.perform(get("/api/v1/destinations/{destinationId}/likes", destId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").value(true))
                .andExpect(jsonPath("$.likesCount").value(10));
    }

    @Test
    void getStatus_shouldReturn200WhenAnonymous() throws Exception {
        var destId = UUID.randomUUID();

        when(likeService.getStatus(eq(destId), isNull())).thenReturn(new LikeResponse(false, 5L));

        mockMvc.perform(get("/api/v1/destinations/{destinationId}/likes", destId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").value(false));
    }

    @Test
    @WithMockUser
    void toggle_shouldReturn200() throws Exception {
        var destId = UUID.randomUUID();

        when(likeService.toggle(eq(destId), any())).thenReturn(new LikeResponse(true, 11L));

        mockMvc.perform(post("/api/v1/destinations/{destinationId}/likes", destId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").value(true))
                .andExpect(jsonPath("$.likesCount").value(11));
    }

    @Test
    void toggle_shouldReturn401WhenAnonymous() throws Exception {
        var destId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/destinations/{destinationId}/likes", destId))
                .andExpect(status().isUnauthorized());
    }
}
