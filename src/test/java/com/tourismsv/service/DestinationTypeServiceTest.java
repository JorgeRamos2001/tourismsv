package com.tourismsv.service;

import com.tourismsv.domain.entity.DestinationType;
import com.tourismsv.dto.request.DestinationTypeRequest;
import com.tourismsv.dto.response.DestinationTypeResponse;
import com.tourismsv.exception.ConflictException;
import com.tourismsv.exception.ResourceNotFoundException;
import com.tourismsv.repository.DestinationTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DestinationTypeServiceTest {

    @Mock
    private DestinationTypeRepository repository;

    private DestinationTypeService service;

    @BeforeEach
    void setUp() {
        service = new DestinationTypeService(repository);
    }

    @Test
    void findAll_shouldReturnListOfResponses() {
        var id1 = UUID.randomUUID();
        var id2 = UUID.randomUUID();
        var now = LocalDateTime.now();

        var entity1 = DestinationType.builder()
                .id(id1)
                .name("Beach")
                .description("Beach destinations")
                .createdAt(now)
                .build();
        var entity2 = DestinationType.builder()
                .id(id2)
                .name("Mountain")
                .description("Mountain destinations")
                .createdAt(now)
                .build();

        when(repository.findAll()).thenReturn(List.of(entity1, entity2));

        var result = service.findAll();

        assertEquals(2, result.size());
        assertEquals(id1, result.get(0).id());
        assertEquals("Beach", result.get(0).name());
        assertEquals("Beach destinations", result.get(0).description());
        assertEquals(now, result.get(0).createdAt());
        assertEquals(id2, result.get(1).id());
        assertEquals("Mountain", result.get(1).name());
    }

    @Test
    void findAll_shouldReturnEmptyListWhenNoneExist() {
        when(repository.findAll()).thenReturn(List.of());

        var result = service.findAll();

        assertTrue(result.isEmpty());
    }

    @Test
    void findById_shouldReturnResponseWhenFound() {
        var id = UUID.randomUUID();
        var now = LocalDateTime.now();
        var entity = DestinationType.builder()
                .id(id)
                .name("Beach")
                .description("Beach destinations")
                .createdAt(now)
                .build();

        when(repository.findById(id)).thenReturn(Optional.of(entity));

        var result = service.findById(id);

        assertEquals(id, result.id());
        assertEquals("Beach", result.name());
        assertEquals("Beach destinations", result.description());
        assertEquals(now, result.createdAt());
    }

    @Test
    void findById_shouldThrowResourceNotFoundExceptionWhenNotFound() {
        var id = UUID.randomUUID();

        when(repository.findById(id)).thenReturn(Optional.empty());

        var ex = assertThrows(ResourceNotFoundException.class, () -> service.findById(id));

        assertTrue(ex.getMessage().contains("DestinationType"));
        assertTrue(ex.getMessage().contains(id.toString()));
    }

    @Test
    void create_shouldSaveAndReturnResponse() {
        var request = new DestinationTypeRequest("Beach", "Beach destinations");
        var id = UUID.randomUUID();
        var now = LocalDateTime.now();

        when(repository.existsByName("Beach")).thenReturn(false);
        when(repository.save(any(DestinationType.class))).thenAnswer(invocation -> {
            var entity = invocation.<DestinationType>getArgument(0);
            return DestinationType.builder()
                    .id(id)
                    .name(entity.getName())
                    .description(entity.getDescription())
                    .createdAt(now)
                    .build();
        });

        var result = service.create(request);

        assertEquals(id, result.id());
        assertEquals("Beach", result.name());
        assertEquals("Beach destinations", result.description());
        assertEquals(now, result.createdAt());

        verify(repository).existsByName("Beach");
        verify(repository).save(any(DestinationType.class));
    }

    @Test
    void create_shouldThrowConflictExceptionWhenNameAlreadyExists() {
        var request = new DestinationTypeRequest("Beach", "Beach destinations");

        when(repository.existsByName("Beach")).thenReturn(true);

        var ex = assertThrows(ConflictException.class, () -> service.create(request));

        assertTrue(ex.getMessage().contains("Beach"));
        verify(repository, never()).save(any());
    }

    @Test
    void update_shouldUpdateAndReturnResponse() {
        var id = UUID.randomUUID();
        var now = LocalDateTime.now();
        var existing = DestinationType.builder()
                .id(id)
                .name("Beach")
                .description("Old description")
                .createdAt(now)
                .build();
        var request = new DestinationTypeRequest("Beach Updated", "Updated description");

        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.existsByName("Beach Updated")).thenReturn(false);
        when(repository.save(any(DestinationType.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.update(id, request);

        assertEquals(id, result.id());
        assertEquals("Beach Updated", result.name());
        assertEquals("Updated description", result.description());
        assertEquals(now, result.createdAt());

        verify(repository).findById(id);
        verify(repository).existsByName("Beach Updated");
        verify(repository).save(existing);
    }

    @Test
    void update_shouldThrowResourceNotFoundExceptionWhenNotFound() {
        var id = UUID.randomUUID();
        var request = new DestinationTypeRequest("Beach", "Beach destinations");

        when(repository.findById(id)).thenReturn(Optional.empty());

        var ex = assertThrows(ResourceNotFoundException.class, () -> service.update(id, request));

        assertTrue(ex.getMessage().contains("DestinationType"));
        assertTrue(ex.getMessage().contains(id.toString()));
        verify(repository, never()).existsByName(any());
        verify(repository, never()).save(any());
    }

    @Test
    void update_shouldThrowConflictExceptionWhenNameAlreadyExistsAndDifferent() {
        var id = UUID.randomUUID();
        var existing = DestinationType.builder()
                .id(id)
                .name("Beach")
                .description("Beach destinations")
                .createdAt(LocalDateTime.now())
                .build();
        var request = new DestinationTypeRequest("Mountain", "Mountain destinations");

        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.existsByName("Mountain")).thenReturn(true);

        var ex = assertThrows(ConflictException.class, () -> service.update(id, request));

        assertTrue(ex.getMessage().contains("Mountain"));
        verify(repository, never()).save(any());
    }

    @Test
    void update_shouldAllowSameNameWhenNameUnchanged() {
        var id = UUID.randomUUID();
        var now = LocalDateTime.now();
        var existing = DestinationType.builder()
                .id(id)
                .name("Beach")
                .description("Beach destinations")
                .createdAt(now)
                .build();
        var request = new DestinationTypeRequest("Beach", "Updated description");

        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any(DestinationType.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.update(id, request);

        assertEquals("Beach", result.name());
        assertEquals("Updated description", result.description());

        verify(repository).findById(id);
        verify(repository, never()).existsByName(any());
        verify(repository).save(existing);
    }

    @Test
    void delete_shouldDeleteWhenExists() {
        var id = UUID.randomUUID();

        when(repository.existsById(id)).thenReturn(true);

        service.delete(id);

        verify(repository).deleteById(id);
    }

    @Test
    void delete_shouldThrowResourceNotFoundExceptionWhenNotFound() {
        var id = UUID.randomUUID();

        when(repository.existsById(id)).thenReturn(false);

        var ex = assertThrows(ResourceNotFoundException.class, () -> service.delete(id));

        assertTrue(ex.getMessage().contains("DestinationType"));
        assertTrue(ex.getMessage().contains(id.toString()));
        verify(repository, never()).deleteById(any());
    }
}
