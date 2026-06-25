package com.tourismsv.service;

import com.tourismsv.domain.entity.Destination;
import com.tourismsv.domain.entity.DestinationImage;
import com.tourismsv.domain.entity.DestinationType;
import com.tourismsv.domain.enums.DestinationState;
import com.tourismsv.dto.request.DestinationRequest;
import com.tourismsv.dto.response.DestinationImageResponse;
import com.tourismsv.dto.response.DestinationResponse;
import com.tourismsv.exception.ConflictException;
import com.tourismsv.exception.ResourceNotFoundException;
import com.tourismsv.repository.DestinationImageRepository;
import com.tourismsv.repository.DestinationLikeRepository;
import com.tourismsv.repository.DestinationRepository;
import com.tourismsv.repository.DestinationReviewRepository;
import com.tourismsv.repository.DestinationSaveRepository;
import com.tourismsv.repository.DestinationTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DestinationServiceTest {

    @Mock
    private DestinationRepository destinationRepository;
    @Mock
    private DestinationImageRepository imageRepository;
    @Mock
    private DestinationLikeRepository likeRepository;
    @Mock
    private DestinationSaveRepository saveRepository;
    @Mock
    private DestinationReviewRepository reviewRepository;
    @Mock
    private DestinationTypeRepository destinationTypeRepository;

    private DestinationService service;
    private DestinationType destinationType;
    private Destination destination;
    private DestinationRequest request;
    private UUID destinationId;
    private UUID typeId;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        service = new DestinationService(destinationRepository, imageRepository, likeRepository,
                saveRepository, reviewRepository, destinationTypeRepository);

        destinationId = UUID.randomUUID();
        typeId = UUID.randomUUID();
        pageable = PageRequest.of(0, 10);

        destinationType = DestinationType.builder()
                .id(typeId)
                .name("Beach")
                .build();

        destination = Destination.builder()
                .id(destinationId)
                .name("El Tunco")
                .description("Beautiful beach")
                .country("El Salvador")
                .city("La Libertad")
                .latitude(BigDecimal.valueOf(13.5))
                .longitude(BigDecimal.valueOf(-89.3))
                .destinationType(destinationType)
                .urlBanner("https://example.com/banner.jpg")
                .state(DestinationState.DRAFT)
                .createdAt(LocalDateTime.now())
                .build();

        request = new DestinationRequest(
                "El Tunco",
                "Beautiful beach",
                "El Salvador",
                "La Libertad",
                BigDecimal.valueOf(13.5),
                BigDecimal.valueOf(-89.3),
                typeId,
                "https://example.com/banner.jpg"
        );
    }

    private void stubToResponse(Destination dest, long likeCount, long saveCount, Double avgRating) {
        when(likeRepository.countByDestinationId(dest.getId())).thenReturn(likeCount);
        when(saveRepository.countByDestinationId(dest.getId())).thenReturn(saveCount);
        when(reviewRepository.findAverageRatingByDestinationId(dest.getId())).thenReturn(avgRating);
    }

    @Test
    void findAll_withAllFilters_shouldReturnMappedPage() {
        var state = DestinationState.ACTIVE;
        var stateStr = state.name();
        stubToResponse(destination, 5L, 3L, 4.5);
        var page = new PageImpl<>(List.of(destination));
        when(destinationRepository.search("Tunco", stateStr, typeId, "El Salvador", "La Libertad", pageable))
                .thenReturn(page);

        Page<DestinationResponse> result = service.findAll("Tunco", state, typeId, "El Salvador", "La Libertad", pageable);

        assertEquals(1, result.getTotalElements());
        var dto = result.getContent().get(0);
        assertEquals(destinationId, dto.id());
        assertEquals("El Tunco", dto.name());
        assertEquals(5L, dto.likeCount());
        assertEquals(3L, dto.saveCount());
        assertEquals(4.5, dto.avgRating());
        verify(destinationRepository).search("Tunco", stateStr, typeId, "El Salvador", "La Libertad", pageable);
    }

    @Test
    void findAll_withNullFilters_shouldReturnMappedPage() {
        stubToResponse(destination, 0L, 0L, null);
        var page = new PageImpl<>(List.of(destination));
        when(destinationRepository.search(null, null, null, null, null, pageable))
                .thenReturn(page);

        Page<DestinationResponse> result = service.findAll(null, null, null, null, null, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(0L, result.getContent().get(0).likeCount());
        assertEquals(0L, result.getContent().get(0).saveCount());
        assertEquals(0.0, result.getContent().get(0).avgRating());
        verify(destinationRepository).search(null, null, null, null, null, pageable);
    }

    @Test
    void findAll_emptyResult_shouldReturnEmptyPage() {
        when(destinationRepository.search(null, null, null, null, null, pageable))
                .thenReturn(Page.empty());

        Page<DestinationResponse> result = service.findAll(null, null, null, null, null, pageable);

        assertTrue(result.isEmpty());
        verify(destinationRepository).search(null, null, null, null, null, pageable);
    }

    @Test
    void findById_whenFound_shouldReturnMappedResponse() {
        stubToResponse(destination, 10L, 5L, 4.8);
        when(destinationRepository.findById(destinationId)).thenReturn(Optional.of(destination));

        DestinationResponse result = service.findById(destinationId);

        assertEquals(destinationId, result.id());
        assertEquals("El Tunco", result.name());
        assertEquals("Beach", result.destinationTypeName());
        assertEquals(10L, result.likeCount());
        assertEquals(5L, result.saveCount());
        assertEquals(4.8, result.avgRating());
        verify(destinationRepository).findById(destinationId);
    }

    @Test
    void findById_whenNotFound_shouldThrowResourceNotFoundException() {
        when(destinationRepository.findById(destinationId)).thenReturn(Optional.empty());

        var ex = assertThrows(ResourceNotFoundException.class, () -> service.findById(destinationId));

        assertTrue(ex.getMessage().contains("Destination"));
        assertTrue(ex.getMessage().contains(destinationId.toString()));
        verify(destinationRepository).findById(destinationId);
    }

    @Test
    void create_whenNameNotTaken_shouldSaveAndReturnResponse() {
        when(destinationRepository.existsByName("El Tunco")).thenReturn(false);
        when(destinationTypeRepository.getReferenceById(typeId)).thenReturn(destinationType);
        stubToResponse(destination, 0L, 0L, null);
        when(destinationRepository.save(any(Destination.class))).thenAnswer(invocation -> {
            var saved = invocation.<Destination>getArgument(0);
            saved.setId(destinationId);
            saved.setCreatedAt(LocalDateTime.now());
            return saved;
        });

        DestinationResponse result = service.create(request);

        assertEquals(DestinationState.DRAFT, result.state());
        assertEquals("El Tunco", result.name());
        assertEquals("Beach", result.destinationTypeName());
        verify(destinationRepository).existsByName("El Tunco");
        verify(destinationTypeRepository).getReferenceById(typeId);
        verify(destinationRepository).save(any(Destination.class));
    }

    @Test
    void create_whenNameAlreadyTaken_shouldThrowConflictException() {
        when(destinationRepository.existsByName("El Tunco")).thenReturn(true);

        var ex = assertThrows(ConflictException.class, () -> service.create(request));

        assertTrue(ex.getMessage().contains("El Tunco"));
        verify(destinationRepository).existsByName("El Tunco");
        verify(destinationRepository, never()).save(any());
    }

    @Test
    void update_whenFoundAndNameChanged_shouldUpdateAndReturnResponse() {
        var existing = Destination.builder()
                .id(destinationId)
                .name("Old Name")
                .description("Old description")
                .country("Old country")
                .city("Old city")
                .latitude(BigDecimal.ZERO)
                .longitude(BigDecimal.ZERO)
                .destinationType(destinationType)
                .urlBanner("https://example.com/old.jpg")
                .state(DestinationState.DRAFT)
                .createdAt(LocalDateTime.now())
                .build();

        when(destinationRepository.findById(destinationId)).thenReturn(Optional.of(existing));
        when(destinationRepository.existsByName("El Tunco")).thenReturn(false);
        when(destinationTypeRepository.getReferenceById(typeId)).thenReturn(destinationType);
        stubToResponse(existing, 2L, 1L, 3.5);
        when(destinationRepository.save(existing)).thenReturn(existing);

        DestinationResponse result = service.update(destinationId, request);

        assertEquals("El Tunco", result.name());
        assertEquals("Beautiful beach", result.description());
        assertEquals("El Salvador", result.country());
        assertEquals("La Libertad", result.city());
        assertEquals(2L, result.likeCount());
        assertEquals(1L, result.saveCount());
        assertEquals(3.5, result.avgRating());
        verify(destinationRepository).findById(destinationId);
        verify(destinationRepository).existsByName("El Tunco");
        verify(destinationTypeRepository).getReferenceById(typeId);
        verify(destinationRepository).save(existing);
    }

    @Test
    void update_whenNotFound_shouldThrowResourceNotFoundException() {
        when(destinationRepository.findById(destinationId)).thenReturn(Optional.empty());

        var ex = assertThrows(ResourceNotFoundException.class, () -> service.update(destinationId, request));

        assertTrue(ex.getMessage().contains("Destination"));
        assertTrue(ex.getMessage().contains(destinationId.toString()));
        verify(destinationRepository).findById(destinationId);
        verify(destinationRepository, never()).existsByName(any());
        verify(destinationRepository, never()).save(any());
    }

    @Test
    void update_whenNameConflictsWithAnother_shouldThrowConflictException() {
        var existing = Destination.builder()
                .id(destinationId)
                .name("Old Name")
                .description("Old description")
                .country("Old country")
                .city("Old city")
                .latitude(BigDecimal.ZERO)
                .longitude(BigDecimal.ZERO)
                .destinationType(destinationType)
                .urlBanner("https://example.com/old.jpg")
                .state(DestinationState.DRAFT)
                .createdAt(LocalDateTime.now())
                .build();

        when(destinationRepository.findById(destinationId)).thenReturn(Optional.of(existing));
        when(destinationRepository.existsByName("El Tunco")).thenReturn(true);

        var ex = assertThrows(ConflictException.class, () -> service.update(destinationId, request));

        assertTrue(ex.getMessage().contains("El Tunco"));
        verify(destinationRepository).findById(destinationId);
        verify(destinationRepository).existsByName("El Tunco");
        verify(destinationRepository, never()).save(any());
    }

    @Test
    void delete_whenExists_shouldDeleteById() {
        when(destinationRepository.existsById(destinationId)).thenReturn(true);

        service.delete(destinationId);

        verify(destinationRepository).existsById(destinationId);
        verify(destinationRepository).deleteById(destinationId);
    }

    @Test
    void delete_whenNotFound_shouldThrowResourceNotFoundException() {
        when(destinationRepository.existsById(destinationId)).thenReturn(false);

        var ex = assertThrows(ResourceNotFoundException.class, () -> service.delete(destinationId));

        assertTrue(ex.getMessage().contains("Destination"));
        assertTrue(ex.getMessage().contains(destinationId.toString()));
        verify(destinationRepository).existsById(destinationId);
        verify(destinationRepository, never()).deleteById(any());
    }

    @Test
    void findImagesByDestinationId_whenDestinationFound_shouldReturnImageList() {
        var imageId1 = UUID.randomUUID();
        var imageId2 = UUID.randomUUID();
        var image1 = DestinationImage.builder().id(imageId1).urlImage("https://example.com/img1.jpg").build();
        var image2 = DestinationImage.builder().id(imageId2).urlImage("https://example.com/img2.jpg").build();

        when(destinationRepository.existsById(destinationId)).thenReturn(true);
        when(imageRepository.findByDestinationId(destinationId)).thenReturn(List.of(image1, image2));

        List<DestinationImageResponse> result = service.findImagesByDestinationId(destinationId);

        assertEquals(2, result.size());
        assertEquals(imageId1, result.get(0).id());
        assertEquals("https://example.com/img1.jpg", result.get(0).urlImage());
        assertEquals(imageId2, result.get(1).id());
        assertEquals("https://example.com/img2.jpg", result.get(1).urlImage());
        verify(destinationRepository).existsById(destinationId);
        verify(imageRepository).findByDestinationId(destinationId);
    }

    @Test
    void findImagesByDestinationId_whenDestinationNotFound_shouldThrowResourceNotFoundException() {
        when(destinationRepository.existsById(destinationId)).thenReturn(false);

        var ex = assertThrows(ResourceNotFoundException.class,
                () -> service.findImagesByDestinationId(destinationId));

        assertTrue(ex.getMessage().contains("Destination"));
        assertTrue(ex.getMessage().contains(destinationId.toString()));
        verify(destinationRepository).existsById(destinationId);
        verify(imageRepository, never()).findByDestinationId(any());
    }

    @Test
    void addImage_whenDestinationFound_shouldSaveAndReturnResponse() {
        var urlImage = "https://example.com/new.jpg";
        var imageId = UUID.randomUUID();
        var savedImage = DestinationImage.builder()
                .id(imageId)
                .destination(destination)
                .urlImage(urlImage)
                .build();

        when(destinationRepository.findById(destinationId)).thenReturn(Optional.of(destination));
        when(imageRepository.save(any(DestinationImage.class))).thenReturn(savedImage);

        DestinationImageResponse result = service.addImage(destinationId, urlImage);

        assertEquals(imageId, result.id());
        assertEquals(urlImage, result.urlImage());
        verify(destinationRepository).findById(destinationId);
        verify(imageRepository).save(any(DestinationImage.class));
    }

    @Test
    void addImage_whenDestinationNotFound_shouldThrowResourceNotFoundException() {
        when(destinationRepository.findById(destinationId)).thenReturn(Optional.empty());

        var ex = assertThrows(ResourceNotFoundException.class,
                () -> service.addImage(destinationId, "https://example.com/img.jpg"));

        assertTrue(ex.getMessage().contains("Destination"));
        assertTrue(ex.getMessage().contains(destinationId.toString()));
        verify(destinationRepository).findById(destinationId);
        verify(imageRepository, never()).save(any());
    }

    @Test
    void deleteImage_whenImageBelongsToDestination_shouldDelete() {
        var imageId = UUID.randomUUID();
        var imageDest = Destination.builder().id(destinationId).build();
        var image = DestinationImage.builder()
                .id(imageId)
                .destination(imageDest)
                .build();

        when(imageRepository.findById(imageId)).thenReturn(Optional.of(image));

        service.deleteImage(destinationId, imageId);

        verify(imageRepository).findById(imageId);
        verify(imageRepository).delete(image);
    }

    @Test
    void deleteImage_whenImageNotFound_shouldThrowResourceNotFoundException() {
        var imageId = UUID.randomUUID();

        when(imageRepository.findById(imageId)).thenReturn(Optional.empty());

        var ex = assertThrows(ResourceNotFoundException.class,
                () -> service.deleteImage(destinationId, imageId));

        assertTrue(ex.getMessage().contains("DestinationImage"));
        assertTrue(ex.getMessage().contains(imageId.toString()));
        verify(imageRepository).findById(imageId);
        verify(imageRepository, never()).delete(any());
    }

    @Test
    void deleteImage_whenImageDoesNotBelongToDestination_shouldThrowResourceNotFoundException() {
        var imageId = UUID.randomUUID();
        var otherDestinationId = UUID.randomUUID();
        var otherDest = Destination.builder().id(otherDestinationId).build();
        var image = DestinationImage.builder()
                .id(imageId)
                .destination(otherDest)
                .build();

        when(imageRepository.findById(imageId)).thenReturn(Optional.of(image));

        var ex = assertThrows(ResourceNotFoundException.class,
                () -> service.deleteImage(destinationId, imageId));

        assertTrue(ex.getMessage().contains("DestinationImage"));
        assertTrue(ex.getMessage().contains(imageId.toString()));
        verify(imageRepository).findById(imageId);
        verify(imageRepository, never()).delete(any());
    }
}
