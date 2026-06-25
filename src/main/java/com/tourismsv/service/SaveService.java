package com.tourismsv.service;

import com.tourismsv.domain.entity.Destination;
import com.tourismsv.domain.entity.User;
import com.tourismsv.dto.response.SaveResponse;
import com.tourismsv.exception.ResourceNotFoundException;
import com.tourismsv.repository.DestinationRepository;
import com.tourismsv.repository.DestinationSaveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SaveService {

    private final DestinationSaveRepository saveRepository;
    private final DestinationRepository destinationRepository;

    @Transactional(readOnly = true)
    public SaveResponse getStatus(UUID destinationId, User user) {
        if (!destinationRepository.existsById(destinationId)) {
            throw new ResourceNotFoundException("Destination", "id", destinationId);
        }

        var saved = user != null && saveRepository.existsByDestinationIdAndUserId(destinationId, user.getId());
        var count = saveRepository.countByDestinationId(destinationId);

        return new SaveResponse(saved, count);
    }

    @Transactional
    public SaveResponse toggle(UUID destinationId, User user) {
        var destination = destinationRepository.findById(destinationId)
                .orElseThrow(() -> new ResourceNotFoundException("Destination", "id", destinationId));

        var existing = saveRepository.findByDestinationIdAndUserId(destinationId, user.getId());

        if (existing.isPresent()) {
            saveRepository.delete(existing.get());
        } else {
            var save = com.tourismsv.domain.entity.DestinationSave.builder()
                    .destination(destination)
                    .user(user)
                    .build();
            saveRepository.save(save);
        }

        var saved = existing.isEmpty();
        var count = saveRepository.countByDestinationId(destinationId);

        return new SaveResponse(saved, count);
    }

    @Transactional(readOnly = true)
    public Page<com.tourismsv.dto.response.DestinationResponse> findSavedDestinations(User user, Pageable pageable) {
        var saves = saveRepository.findByUserId(user.getId(), pageable);
        return saves.map(s -> {
            var d = s.getDestination();
            return new com.tourismsv.dto.response.DestinationResponse(
                    d.getId(), d.getName(), d.getDescription(),
                    d.getCountry(), d.getCity(), d.getLatitude(), d.getLongitude(),
                    d.getDestinationType().getName(), d.getUrlBanner(), d.getState(),
                    d.getCreatedAt(), 0, 1, 0.0
            );
        });
    }
}
