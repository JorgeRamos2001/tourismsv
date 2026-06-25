package com.tourismsv.controller;

import tools.jackson.databind.ObjectMapper;
import com.tourismsv.domain.entity.User;
import com.tourismsv.domain.enums.DestinationState;
import com.tourismsv.domain.enums.Role;
import com.tourismsv.domain.enums.UserState;
import com.tourismsv.dto.response.DestinationResponse;
import com.tourismsv.dto.response.SaveResponse;
import com.tourismsv.repository.UserRepository;
import com.tourismsv.security.JwtService;
import com.tourismsv.service.SaveService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SaveController.class)
@Import(TestSecurityConfig.class)
class SaveControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SaveService saveService;

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

        when(saveService.getStatus(eq(destId), any())).thenReturn(new SaveResponse(true, 10L));

        mockMvc.perform(get("/api/v1/destinations/{destinationId}/saves", destId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saved").value(true))
                .andExpect(jsonPath("$.savesCount").value(10));
    }

    @Test
    void getStatus_shouldReturn200WhenAnonymous() throws Exception {
        var destId = UUID.randomUUID();

        when(saveService.getStatus(eq(destId), isNull())).thenReturn(new SaveResponse(false, 5L));

        mockMvc.perform(get("/api/v1/destinations/{destinationId}/saves", destId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saved").value(false));
    }

    @Test
    @WithMockUser
    void toggle_shouldReturn200() throws Exception {
        var destId = UUID.randomUUID();

        when(saveService.toggle(eq(destId), any())).thenReturn(new SaveResponse(true, 11L));

        mockMvc.perform(post("/api/v1/destinations/{destinationId}/saves", destId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saved").value(true))
                .andExpect(jsonPath("$.savesCount").value(11));
    }

    @Test
    void toggle_shouldReturn401WhenAnonymous() throws Exception {
        var destId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/destinations/{destinationId}/saves", destId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void listSaved_shouldReturn200AndPage() throws Exception {
        var dest = new DestinationResponse(
                UUID.randomUUID(), "El Tunco", "Surf beach",
                "El Salvador", "La Libertad",
                new BigDecimal("13.48"), new BigDecimal("-89.32"),
                "Beach", null, DestinationState.ACTIVE,
                LocalDateTime.now(), 10, 1, 4.5
        );
        var page = new PageImpl<>(List.of(dest));

        when(saveService.findSavedDestinations(any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/destinations/{destinationId}/saves/list", UUID.randomUUID())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("El Tunco"));
    }

    @Test
    void listSaved_shouldReturn401WhenAnonymous() throws Exception {
        mockMvc.perform(get("/api/v1/destinations/{destinationId}/saves/list", UUID.randomUUID())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isUnauthorized());
    }
}
