package com.tourismsv.controller;

import tools.jackson.databind.ObjectMapper;
import com.tourismsv.domain.entity.User;
import com.tourismsv.domain.enums.Role;
import com.tourismsv.domain.enums.UserState;
import com.tourismsv.dto.request.UserStateUpdateRequest;
import com.tourismsv.dto.request.UserUpdateRequest;
import com.tourismsv.dto.response.UserResponse;
import com.tourismsv.repository.UserRepository;
import com.tourismsv.security.JwtService;
import com.tourismsv.service.UserService;
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

@WebMvcTest(UserController.class)
@Import(TestSecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

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

    private UserResponse sampleUser() {
        return new UserResponse(UUID.randomUUID(), "Test User", "test@example.com",
                null, Role.TOURIST, UserState.ACTIVE, LocalDateTime.now());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void findAll_shouldReturn200AndPage() throws Exception {
        var page = new PageImpl<>(List.of(sampleUser()));

        when(userService.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    @WithMockUser(roles = "TOURIST")
    void findAll_shouldReturn403WhenTourist() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isForbidden());
    }

    @Test
    void findAll_shouldReturn401WhenAnonymous() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void findById_shouldReturn200WhenAdmin() throws Exception {
        var id = UUID.randomUUID();
        var user = new UserResponse(id, "Test User", "test@example.com",
                null, Role.TOURIST, UserState.ACTIVE, LocalDateTime.now());

        when(userService.findById(id)).thenReturn(user);

        mockMvc.perform(get("/api/v1/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @WithMockUser(roles = "TOURIST")
    void findById_shouldReturn403WhenTourist() throws Exception {
        mockMvc.perform(get("/api/v1/users/{id}", UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void findById_shouldReturn404WhenNotExists() throws Exception {
        var id = UUID.randomUUID();

        when(userService.findById(id))
                .thenThrow(new com.tourismsv.exception.ResourceNotFoundException("User", "id", id));

        mockMvc.perform(get("/api/v1/users/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void me_shouldReturn200() throws Exception {
        var user = sampleUser();

        when(userService.toResponse(any())).thenReturn(user);

        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void me_shouldReturn401WhenAnonymous() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void update_shouldReturn200() throws Exception {
        var id = UUID.randomUUID();
        var request = new UserUpdateRequest("Updated Name", "updated@example.com", null);
        var updatedUser = new UserResponse(id, "Updated Name", "updated@example.com",
                null, Role.TOURIST, UserState.ACTIVE, LocalDateTime.now());

        when(userService.update(eq(id), any(UserUpdateRequest.class), any())).thenReturn(updatedUser);

        mockMvc.perform(put("/api/v1/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    void update_shouldReturn401WhenAnonymous() throws Exception {
        var id = UUID.randomUUID();
        var request = new UserUpdateRequest("Updated Name", "updated@example.com", null);

        mockMvc.perform(put("/api/v1/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void update_shouldReturn400WhenEmailInvalid() throws Exception {
        var id = UUID.randomUUID();
        var request = new UserUpdateRequest("Updated Name", "invalid-email", null);

        mockMvc.perform(put("/api/v1/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateState_shouldReturn204WhenAdmin() throws Exception {
        var id = UUID.randomUUID();
        var request = new UserStateUpdateRequest(UserState.INACTIVE);

        doNothing().when(userService).updateState(id, UserState.INACTIVE);

        mockMvc.perform(patch("/api/v1/users/{id}/state", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "TOURIST")
    void updateState_shouldReturn403WhenTourist() throws Exception {
        var id = UUID.randomUUID();
        var request = new UserStateUpdateRequest(UserState.INACTIVE);

        mockMvc.perform(patch("/api/v1/users/{id}/state", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_shouldReturn204WhenAdmin() throws Exception {
        var id = UUID.randomUUID();

        doNothing().when(userService).delete(id);

        mockMvc.perform(delete("/api/v1/users/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "TOURIST")
    void delete_shouldReturn403WhenTourist() throws Exception {
        var id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/users/{id}", id))
                .andExpect(status().isForbidden());
    }
}
