package com.tourismsv.controller;

import com.tourismsv.dto.request.DestinationTypeRequest;
import com.tourismsv.dto.response.DestinationTypeResponse;
import com.tourismsv.service.DestinationTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/destination-types")
@RequiredArgsConstructor
public class DestinationTypeController {

    private final DestinationTypeService destinationTypeService;

    @GetMapping
    public ResponseEntity<List<DestinationTypeResponse>> findAll() {
        return ResponseEntity.ok(destinationTypeService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DestinationTypeResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(destinationTypeService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DestinationTypeResponse> create(@Valid @RequestBody DestinationTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(destinationTypeService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DestinationTypeResponse> update(@PathVariable UUID id,
                                                           @Valid @RequestBody DestinationTypeRequest request) {
        return ResponseEntity.ok(destinationTypeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        destinationTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
