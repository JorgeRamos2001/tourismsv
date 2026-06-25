package com.tourismsv.controller;

import com.tourismsv.domain.enums.DestinationState;
import com.tourismsv.dto.request.DestinationRequest;
import com.tourismsv.dto.response.DestinationImageResponse;
import com.tourismsv.dto.response.DestinationResponse;
import com.tourismsv.service.DestinationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/destinations")
@RequiredArgsConstructor
public class DestinationController {

    private final DestinationService destinationService;

    @GetMapping
    public ResponseEntity<Page<DestinationResponse>> findAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) DestinationState state,
            @RequestParam(required = false) UUID destinationTypeId,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String city,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(
                destinationService.findAll(name, state, destinationTypeId, country, city, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DestinationResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(destinationService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DestinationResponse> create(@Valid @RequestBody DestinationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(destinationService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DestinationResponse> update(@PathVariable UUID id,
                                                       @Valid @RequestBody DestinationRequest request) {
        return ResponseEntity.ok(destinationService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        destinationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/images")
    public ResponseEntity<List<DestinationImageResponse>> findImages(@PathVariable UUID id) {
        return ResponseEntity.ok(destinationService.findImagesByDestinationId(id));
    }

    @PostMapping("/{id}/images")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DestinationImageResponse> addImage(@PathVariable UUID id,
                                                              @RequestBody Map<String, String> body) {
        var urlImage = body.get("urlImage");
        if (urlImage == null || urlImage.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(destinationService.addImage(id, urlImage));
    }

    @DeleteMapping("/{id}/images/{imageId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteImage(@PathVariable UUID id, @PathVariable UUID imageId) {
        destinationService.deleteImage(id, imageId);
        return ResponseEntity.noContent().build();
    }
}
