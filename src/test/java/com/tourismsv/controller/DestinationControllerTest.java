package com.tourismsv.controller;

import tools.jackson.databind.ObjectMapper;
import com.tourismsv.domain.enums.DestinationState;
import com.tourismsv.dto.request.DestinationRequest;
import com.tourismsv.dto.response.DestinationImageResponse;
import com.tourismsv.dto.response.DestinationResponse;
import com.tourismsv.security.JwtService;
import com.tourismsv.service.DestinationService;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DestinationController.class)
@Import(TestSecurityConfig.class)
class DestinationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DestinationService destinationService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private DestinationResponse sampleDestination() {
        return new DestinationResponse(
                UUID.randomUUID(), "El Tunco", "Surf beach",
                "El Salvador", "La Libertad",
                new BigDecimal("13.48"), new BigDecimal("-89.32"),
                "Beach", null, DestinationState.ACTIVE,
                LocalDateTime.now(), 10, 5, 4.5
        );
    }

    @Test
    void findAll_shouldReturn200AndPage() throws Exception {
        var page = new PageImpl<>(List.of(sampleDestination()));

        when(destinationService.findAll(isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/destinations")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("El Tunco"));
    }

    @Test
    void findAll_shouldFilterByName() throws Exception {
        when(destinationService.findAll(eq("Tunco"), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/destinations")
                        .param("name", "Tunco")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void findById_shouldReturn200WhenExists() throws Exception {
        var id = UUID.randomUUID();
        var dest = new DestinationResponse(id, "El Tunco", "Surf beach",
                "El Salvador", "La Libertad",
                new BigDecimal("13.48"), new BigDecimal("-89.32"),
                "Beach", null, DestinationState.ACTIVE,
                LocalDateTime.now(), 10, 5, 4.5);

        when(destinationService.findById(id)).thenReturn(dest);

        mockMvc.perform(get("/api/v1/destinations/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("El Tunco"));
    }

    @Test
    void findById_shouldReturn404WhenNotExists() throws Exception {
        var id = UUID.randomUUID();

        when(destinationService.findById(id))
                .thenThrow(new com.tourismsv.exception.ResourceNotFoundException("Destination", "id", id));

        mockMvc.perform(get("/api/v1/destinations/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_shouldReturn201WhenAdmin() throws Exception {
        var request = new DestinationRequest("El Tunco", "Surf beach",
                "El Salvador", "La Libertad",
                new BigDecimal("13.48"), new BigDecimal("-89.32"),
                UUID.randomUUID(), null);

        when(destinationService.create(any(DestinationRequest.class))).thenReturn(sampleDestination());

        mockMvc.perform(post("/api/v1/destinations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("El Tunco"));
    }

    @Test
    @WithMockUser(roles = "TOURIST")
    void create_shouldReturn403WhenTourist() throws Exception {
        var request = new DestinationRequest("El Tunco", "Surf beach",
                "El Salvador", "La Libertad",
                new BigDecimal("13.48"), new BigDecimal("-89.32"),
                UUID.randomUUID(), null);

        mockMvc.perform(post("/api/v1/destinations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_shouldReturn401WhenAnonymous() throws Exception {
        var request = new DestinationRequest("El Tunco", "Surf beach",
                "El Salvador", "La Libertad",
                new BigDecimal("13.48"), new BigDecimal("-89.32"),
                UUID.randomUUID(), null);

        mockMvc.perform(post("/api/v1/destinations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_shouldReturn400WhenNameBlank() throws Exception {
        var request = new DestinationRequest("", "Surf beach",
                "El Salvador", "La Libertad",
                new BigDecimal("13.48"), new BigDecimal("-89.32"),
                UUID.randomUUID(), null);

        mockMvc.perform(post("/api/v1/destinations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void update_shouldReturn200WhenAdmin() throws Exception {
        var id = UUID.randomUUID();
        var request = new DestinationRequest("El Tunco Updated", "Surf beach",
                "El Salvador", "La Libertad",
                new BigDecimal("13.48"), new BigDecimal("-89.32"),
                UUID.randomUUID(), null);

        var updatedDest = new DestinationResponse(id, "El Tunco Updated", "Surf beach",
                "El Salvador", "La Libertad",
                new BigDecimal("13.48"), new BigDecimal("-89.32"),
                "Beach", null, DestinationState.ACTIVE,
                LocalDateTime.now(), 10, 5, 4.5);

        when(destinationService.update(eq(id), any(DestinationRequest.class))).thenReturn(updatedDest);

        mockMvc.perform(put("/api/v1/destinations/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("El Tunco Updated"));
    }

    @Test
    @WithMockUser(roles = "TOURIST")
    void update_shouldReturn403WhenTourist() throws Exception {
        var id = UUID.randomUUID();
        var request = new DestinationRequest("El Tunco Updated", "Surf beach",
                "El Salvador", "La Libertad",
                new BigDecimal("13.48"), new BigDecimal("-89.32"),
                UUID.randomUUID(), null);

        mockMvc.perform(put("/api/v1/destinations/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_shouldReturn204WhenAdmin() throws Exception {
        var id = UUID.randomUUID();

        doNothing().when(destinationService).delete(id);

        mockMvc.perform(delete("/api/v1/destinations/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "TOURIST")
    void delete_shouldReturn403WhenTourist() throws Exception {
        var id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/destinations/{id}", id))
                .andExpect(status().isForbidden());
    }

    @Test
    void findImages_shouldReturn200() throws Exception {
        var destId = UUID.randomUUID();
        var images = List.of(
                new DestinationImageResponse(UUID.randomUUID(), "https://example.com/img1.jpg"),
                new DestinationImageResponse(UUID.randomUUID(), "https://example.com/img2.jpg")
        );

        when(destinationService.findImagesByDestinationId(destId)).thenReturn(images);

        mockMvc.perform(get("/api/v1/destinations/{id}/images", destId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addImage_shouldReturn201WhenAdmin() throws Exception {
        var destId = UUID.randomUUID();
        var imageId = UUID.randomUUID();
        var body = Map.of("urlImage", "https://example.com/img.jpg");

        when(destinationService.addImage(eq(destId), anyString()))
                .thenReturn(new DestinationImageResponse(imageId, "https://example.com/img.jpg"));

        mockMvc.perform(post("/api/v1/destinations/{id}/images", destId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(imageId.toString()));
    }

    @Test
    @WithMockUser(roles = "TOURIST")
    void addImage_shouldReturn403WhenTourist() throws Exception {
        var destId = UUID.randomUUID();
        var body = Map.of("urlImage", "https://example.com/img.jpg");

        mockMvc.perform(post("/api/v1/destinations/{id}/images", destId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteImage_shouldReturn204WhenAdmin() throws Exception {
        var destId = UUID.randomUUID();
        var imageId = UUID.randomUUID();

        doNothing().when(destinationService).deleteImage(destId, imageId);

        mockMvc.perform(delete("/api/v1/destinations/{id}/images/{imageId}", destId, imageId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addImage_shouldReturn400WhenUrlMissing() throws Exception {
        var destId = UUID.randomUUID();
        var body = Map.of("urlImage", "");

        mockMvc.perform(post("/api/v1/destinations/{id}/images", destId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }
}
