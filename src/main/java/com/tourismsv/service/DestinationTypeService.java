package com.tourismsv.service;

import com.tourismsv.domain.entity.DestinationType;
import com.tourismsv.dto.request.DestinationTypeRequest;
import com.tourismsv.dto.response.DestinationTypeResponse;
import com.tourismsv.exception.ConflictException;
import com.tourismsv.exception.ResourceNotFoundException;
import com.tourismsv.repository.DestinationTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DestinationTypeService {

    private final DestinationTypeRepository repository;

    @Transactional(readOnly = true)
    public List<DestinationTypeResponse> findAll() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DestinationTypeResponse findById(UUID id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("DestinationType", "id", id));
    }

    @Transactional
    public DestinationTypeResponse create(DestinationTypeRequest request) {
        if (repository.existsByName(request.name())) {
            throw new ConflictException("Destination type already exists with name: " + request.name());
        }

        var entity = DestinationType.builder()
                .name(request.name())
                .description(request.description())
                .build();

        return toResponse(repository.save(entity));
    }

    @Transactional
    public DestinationTypeResponse update(UUID id, DestinationTypeRequest request) {
        var entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DestinationType", "id", id));

        if (!entity.getName().equals(request.name()) && repository.existsByName(request.name())) {
            throw new ConflictException("Destination type already exists with name: " + request.name());
        }

        entity.setName(request.name());
        entity.setDescription(request.description());

        return toResponse(repository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("DestinationType", "id", id);
        }
        repository.deleteById(id);
    }

    private DestinationTypeResponse toResponse(DestinationType entity) {
        return new DestinationTypeResponse(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getCreatedAt()
        );
    }
}
