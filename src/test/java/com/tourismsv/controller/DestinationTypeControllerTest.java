package com.tourismsv.controller;

import tools.jackson.databind.ObjectMapper;
import com.tourismsv.dto.request.DestinationTypeRequest;
import com.tourismsv.dto.response.DestinationTypeResponse;
import com.tourismsv.security.JwtService;
import com.tourismsv.service.DestinationTypeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DestinationTypeController.class)
@Import(TestSecurityConfig.class)
class DestinationTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DestinationTypeService destinationTypeService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void findAll_shouldReturn200AndList() throws Exception {
        var type1 = new DestinationTypeResponse(UUID.randomUUID(), "Beach", "Beach destinations", LocalDateTime.now());
        var type2 = new DestinationTypeResponse(UUID.randomUUID(), "Mountain", "Mountain destinations", LocalDateTime.now());

        when(destinationTypeService.findAll()).thenReturn(List.of(type1, type2));

        mockMvc.perform(get("/api/v1/destination-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Beach"))
                .andExpect(jsonPath("$[1].name").value("Mountain"));
    }

    @Test
    void findById_shouldReturn200WhenExists() throws Exception {
        var id = UUID.randomUUID();
        var response = new DestinationTypeResponse(id, "Beach", "Beach destinations", LocalDateTime.now());

        when(destinationTypeService.findById(id)).thenReturn(response);

        mockMvc.perform(get("/api/v1/destination-types/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Beach"));
    }

    @Test
    void findById_shouldReturn404WhenNotExists() throws Exception {
        var id = UUID.randomUUID();

        when(destinationTypeService.findById(id)).thenThrow(new com.tourismsv.exception.ResourceNotFoundException("DestinationType", "id", id));

        mockMvc.perform(get("/api/v1/destination-types/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_shouldReturn201WhenAdmin() throws Exception {
        var request = new DestinationTypeRequest("Beach", "Beach destinations");
        var response = new DestinationTypeResponse(UUID.randomUUID(), "Beach", "Beach destinations", LocalDateTime.now());

        when(destinationTypeService.create(any(DestinationTypeRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/destination-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Beach"));
    }

    @Test
    @WithMockUser(roles = "TOURIST")
    void create_shouldReturn403WhenTourist() throws Exception {
        var request = new DestinationTypeRequest("Beach", "Beach destinations");

        mockMvc.perform(post("/api/v1/destination-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_shouldReturn401WhenAnonymous() throws Exception {
        var request = new DestinationTypeRequest("Beach", "Beach destinations");

        mockMvc.perform(post("/api/v1/destination-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_shouldReturn400WhenNameBlank() throws Exception {
        var request = new DestinationTypeRequest("", "Beach destinations");

        mockMvc.perform(post("/api/v1/destination-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_shouldReturn409WhenNameExists() throws Exception {
        var request = new DestinationTypeRequest("Beach", "Beach destinations");

        when(destinationTypeService.create(any(DestinationTypeRequest.class)))
                .thenThrow(new com.tourismsv.exception.ConflictException("DestinationType already exists with name: Beach"));

        mockMvc.perform(post("/api/v1/destination-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void update_shouldReturn200WhenAdmin() throws Exception {
        var id = UUID.randomUUID();
        var request = new DestinationTypeRequest("Beach Updated", "Updated description");
        var response = new DestinationTypeResponse(id, "Beach Updated", "Updated description", LocalDateTime.now());

        when(destinationTypeService.update(eq(id), any(DestinationTypeRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/destination-types/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Beach Updated"));
    }

    @Test
    @WithMockUser(roles = "TOURIST")
    void update_shouldReturn403WhenTourist() throws Exception {
        var id = UUID.randomUUID();
        var request = new DestinationTypeRequest("Beach Updated", "Updated description");

        mockMvc.perform(put("/api/v1/destination-types/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_shouldReturn204WhenAdmin() throws Exception {
        var id = UUID.randomUUID();

        doNothing().when(destinationTypeService).delete(id);

        mockMvc.perform(delete("/api/v1/destination-types/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "TOURIST")
    void delete_shouldReturn403WhenTourist() throws Exception {
        var id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/destination-types/{id}", id))
                .andExpect(status().isForbidden());
    }
}
