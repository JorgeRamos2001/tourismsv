package com.tourismsv.service;

import com.tourismsv.domain.entity.Destination;
import com.tourismsv.domain.entity.DestinationImage;
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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DestinationService {

    private final DestinationRepository destinationRepository;
    private final DestinationImageRepository imageRepository;
    private final DestinationLikeRepository likeRepository;
    private final DestinationSaveRepository saveRepository;
    private final DestinationReviewRepository reviewRepository;
    private final DestinationTypeRepository destinationTypeRepository;

    @Transactional(readOnly = true)
    public Page<DestinationResponse> findAll(String name, DestinationState state, UUID destinationTypeId,
                                              String country, String city, Pageable pageable) {
        var stateStr = state != null ? state.name() : null;
        return destinationRepository.search(name, stateStr, destinationTypeId, country, city, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public DestinationResponse findById(UUID id) {
        var destination = destinationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Destination", "id", id));
        return toResponse(destination);
    }

    @Transactional
    public DestinationResponse create(DestinationRequest request) {
        if (destinationRepository.existsByName(request.name())) {
            throw new ConflictException("Destination already exists with name: " + request.name());
        }

        var destination = Destination.builder()
                .name(request.name())
                .description(request.description())
                .country(request.country())
                .city(request.city())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .destinationType(destinationTypeRepository.getReferenceById(request.destinationTypeId()))
                .urlBanner(request.urlBanner())
                .state(DestinationState.DRAFT)
                .build();

        return toResponse(destinationRepository.save(destination));
    }

    @Transactional
    public DestinationResponse update(UUID id, DestinationRequest request) {
        var destination = destinationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Destination", "id", id));

        if (!destination.getName().equals(request.name()) && destinationRepository.existsByName(request.name())) {
            throw new ConflictException("Destination already exists with name: " + request.name());
        }

        destination.setName(request.name());
        destination.setDescription(request.description());
        destination.setCountry(request.country());
        destination.setCity(request.city());
        destination.setLatitude(request.latitude());
        destination.setLongitude(request.longitude());
        destination.setDestinationType(destinationTypeRepository.getReferenceById(request.destinationTypeId()));
        destination.setUrlBanner(request.urlBanner());

        return toResponse(destinationRepository.save(destination));
    }

    @Transactional
    public void delete(UUID id) {
        if (!destinationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Destination", "id", id);
        }
        destinationRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<DestinationImageResponse> findImagesByDestinationId(UUID destinationId) {
        if (!destinationRepository.existsById(destinationId)) {
            throw new ResourceNotFoundException("Destination", "id", destinationId);
        }
        return imageRepository.findByDestinationId(destinationId).stream()
                .map(img -> new DestinationImageResponse(img.getId(), img.getUrlImage()))
                .toList();
    }

    @Transactional
    public DestinationImageResponse addImage(UUID destinationId, String urlImage) {
        var destination = destinationRepository.findById(destinationId)
                .orElseThrow(() -> new ResourceNotFoundException("Destination", "id", destinationId));

        var image = DestinationImage.builder()
                .destination(destination)
                .urlImage(urlImage)
                .build();

        image = imageRepository.save(image);
        return new DestinationImageResponse(image.getId(), image.getUrlImage());
    }

    @Transactional
    public void deleteImage(UUID destinationId, UUID imageId) {
        var image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("DestinationImage", "id", imageId));

        if (!image.getDestination().getId().equals(destinationId)) {
            throw new ResourceNotFoundException("DestinationImage", "id", imageId);
        }

        imageRepository.delete(image);
    }

    private DestinationResponse toResponse(Destination d) {
        var likeCount = likeRepository.countByDestinationId(d.getId());
        var saveCount = saveRepository.countByDestinationId(d.getId());
        var avgRating = reviewRepository.findAverageRatingByDestinationId(d.getId());

        return new DestinationResponse(
                d.getId(),
                d.getName(),
                d.getDescription(),
                d.getCountry(),
                d.getCity(),
                d.getLatitude(),
                d.getLongitude(),
                d.getDestinationType().getName(),
                d.getUrlBanner(),
                d.getState(),
                d.getCreatedAt(),
                likeCount,
                saveCount,
                avgRating != null ? avgRating : 0.0
        );
    }
}
