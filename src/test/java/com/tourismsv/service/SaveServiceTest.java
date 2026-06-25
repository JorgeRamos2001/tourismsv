package com.tourismsv.service;

import com.tourismsv.domain.entity.Destination;
import com.tourismsv.domain.entity.DestinationSave;
import com.tourismsv.domain.entity.DestinationType;
import com.tourismsv.domain.entity.User;
import com.tourismsv.domain.enums.DestinationState;
import com.tourismsv.domain.enums.Role;
import com.tourismsv.domain.enums.UserState;
import com.tourismsv.dto.response.DestinationResponse;
import com.tourismsv.dto.response.SaveResponse;
import com.tourismsv.exception.ResourceNotFoundException;
import com.tourismsv.repository.DestinationRepository;
import com.tourismsv.repository.DestinationSaveRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaveServiceTest {

    @Mock
    private DestinationSaveRepository saveRepository;

    @Mock
    private DestinationRepository destinationRepository;

    @InjectMocks
    private SaveService saveService;

    private static final UUID DEST_ID = UUID.randomUUID();
    private static final User TEST_USER = User.builder()
            .id(UUID.randomUUID())
            .name("Test User")
            .email("test@example.com")
            .password("pass")
            .role(Role.TOURIST)
            .state(UserState.ACTIVE)
            .build();

    private static final DestinationType TEST_TYPE = DestinationType.builder()
            .id(UUID.randomUUID())
            .name("Beach")
            .description("Beach destination")
            .build();

    private static final Destination TEST_DESTINATION = Destination.builder()
            .id(DEST_ID)
            .name("El Tunco")
            .description("Surf beach")
            .country("El Salvador")
            .city("La Libertad")
            .latitude(new BigDecimal("13.48"))
            .longitude(new BigDecimal("-89.32"))
            .destinationType(TEST_TYPE)
            .urlBanner(null)
            .state(DestinationState.ACTIVE)
            .createdAt(LocalDateTime.now())
            .build();

    @Test
    void getStatus_whenDestinationNotFound_shouldThrow() {
        when(destinationRepository.existsById(DEST_ID)).thenReturn(false);

        var ex = assertThrows(ResourceNotFoundException.class,
                () -> saveService.getStatus(DEST_ID, TEST_USER));
        assertTrue(ex.getMessage().contains("Destination"));
    }

    @Test
    void getStatus_whenAnonymousUser_shouldReturnNotSaved() {
        when(destinationRepository.existsById(DEST_ID)).thenReturn(true);
        when(saveRepository.countByDestinationId(DEST_ID)).thenReturn(5L);

        var response = saveService.getStatus(DEST_ID, null);

        assertFalse(response.saved());
        assertEquals(5L, response.savesCount());
    }

    @Test
    void getStatus_whenUserAndNotSaved_shouldReturnNotSaved() {
        when(destinationRepository.existsById(DEST_ID)).thenReturn(true);
        when(saveRepository.existsByDestinationIdAndUserId(DEST_ID, TEST_USER.getId())).thenReturn(false);
        when(saveRepository.countByDestinationId(DEST_ID)).thenReturn(3L);

        var response = saveService.getStatus(DEST_ID, TEST_USER);

        assertFalse(response.saved());
        assertEquals(3L, response.savesCount());
    }

    @Test
    void getStatus_whenUserAndSaved_shouldReturnSaved() {
        when(destinationRepository.existsById(DEST_ID)).thenReturn(true);
        when(saveRepository.existsByDestinationIdAndUserId(DEST_ID, TEST_USER.getId())).thenReturn(true);
        when(saveRepository.countByDestinationId(DEST_ID)).thenReturn(10L);

        var response = saveService.getStatus(DEST_ID, TEST_USER);

        assertTrue(response.saved());
        assertEquals(10L, response.savesCount());
    }

    @Test
    void toggle_whenDestinationNotFound_shouldThrow() {
        when(destinationRepository.findById(DEST_ID)).thenReturn(Optional.empty());

        var ex = assertThrows(ResourceNotFoundException.class,
                () -> saveService.toggle(DEST_ID, TEST_USER));
        assertTrue(ex.getMessage().contains("Destination"));
    }

    @Test
    void toggle_whenAlreadySaved_shouldDeleteAndReturnNotSaved() {
        var destination = TEST_DESTINATION;
        var existingSave = DestinationSave.builder()
                .id(UUID.randomUUID())
                .destination(destination)
                .user(TEST_USER)
                .build();

        when(destinationRepository.findById(DEST_ID)).thenReturn(Optional.of(destination));
        when(saveRepository.findByDestinationIdAndUserId(DEST_ID, TEST_USER.getId()))
                .thenReturn(Optional.of(existingSave));
        when(saveRepository.countByDestinationId(DEST_ID)).thenReturn(4L);

        var response = saveService.toggle(DEST_ID, TEST_USER);

        verify(saveRepository).delete(existingSave);
        verify(saveRepository, never()).save(any());
        assertFalse(response.saved());
        assertEquals(4L, response.savesCount());
    }

    @Test
    void toggle_whenNotSaved_shouldSaveAndReturnSaved() {
        var destination = TEST_DESTINATION;

        when(destinationRepository.findById(DEST_ID)).thenReturn(Optional.of(destination));
        when(saveRepository.findByDestinationIdAndUserId(DEST_ID, TEST_USER.getId()))
                .thenReturn(Optional.empty());
        when(saveRepository.countByDestinationId(DEST_ID)).thenReturn(7L);

        var response = saveService.toggle(DEST_ID, TEST_USER);

        verify(saveRepository).save(any(DestinationSave.class));
        verify(saveRepository, never()).delete(any());
        assertTrue(response.saved());
        assertEquals(7L, response.savesCount());
    }

    @Test
    void findSavedDestinations_shouldReturnMappedPage() {
        var pageable = PageRequest.of(0, 10);
        var save = DestinationSave.builder()
                .id(UUID.randomUUID())
                .destination(TEST_DESTINATION)
                .user(TEST_USER)
                .build();
        var savesPage = new PageImpl<>(List.of(save));

        when(saveRepository.findByUserId(TEST_USER.getId(), pageable)).thenReturn(savesPage);

        var result = saveService.findSavedDestinations(TEST_USER, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        var dto = result.getContent().getFirst();
        assertEquals(TEST_DESTINATION.getId(), dto.id());
        assertEquals(TEST_DESTINATION.getName(), dto.name());
        assertEquals(TEST_DESTINATION.getDescription(), dto.description());
        assertEquals(TEST_DESTINATION.getCountry(), dto.country());
        assertEquals(TEST_DESTINATION.getCity(), dto.city());
        assertEquals(TEST_DESTINATION.getLatitude(), dto.latitude());
        assertEquals(TEST_DESTINATION.getLongitude(), dto.longitude());
        assertEquals(TEST_TYPE.getName(), dto.destinationTypeName());
        assertNull(dto.urlBanner());
        assertEquals(DestinationState.ACTIVE, dto.state());
        assertNotNull(dto.createdAt());
        assertEquals(0, dto.likeCount());
        assertEquals(1, dto.saveCount());
        assertEquals(0.0, dto.avgRating());
    }

    @Test
    void findSavedDestinations_whenEmpty_shouldReturnEmptyPage() {
        var pageable = PageRequest.of(0, 10);
        var emptyPage = new PageImpl<>(List.<DestinationSave>of());

        when(saveRepository.findByUserId(TEST_USER.getId(), pageable)).thenReturn(emptyPage);

        var result = saveService.findSavedDestinations(TEST_USER, pageable);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }
}
