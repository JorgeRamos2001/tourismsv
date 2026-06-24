package com.tourismsv.controller;

import com.tourismsv.domain.entity.User;
import com.tourismsv.dto.request.ReviewRequest;
import com.tourismsv.dto.response.ReviewResponse;
import com.tourismsv.repository.UserRepository;
import com.tourismsv.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/destinations/{destinationId}/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Page<ReviewResponse>> findByDestination(@PathVariable UUID destinationId,
                                                                   Pageable pageable) {
        return ResponseEntity.ok(reviewService.findByDestinationId(destinationId, pageable));
    }

    @PostMapping
    public ResponseEntity<ReviewResponse> create(@PathVariable UUID destinationId,
                                                  @Valid @RequestBody ReviewRequest request,
                                                  Authentication authentication) {
        var user = userRepository.findByEmail(authentication.getName())
                .orElseThrow();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.create(destinationId, user, request));
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> update(@PathVariable UUID destinationId,
                                                  @PathVariable UUID reviewId,
                                                  @Valid @RequestBody ReviewRequest request,
                                                  Authentication authentication) {
        var user = userRepository.findByEmail(authentication.getName())
                .orElseThrow();
        return ResponseEntity.ok(reviewService.update(destinationId, reviewId, user, request));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> delete(@PathVariable UUID destinationId,
                                        @PathVariable UUID reviewId,
                                        Authentication authentication) {
        var user = userRepository.findByEmail(authentication.getName())
                .orElseThrow();
        reviewService.delete(destinationId, reviewId, user);
        return ResponseEntity.noContent().build();
    }
}
