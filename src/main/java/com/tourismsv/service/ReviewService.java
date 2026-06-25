package com.tourismsv.service;

import com.tourismsv.domain.entity.Destination;
import com.tourismsv.domain.entity.User;
import com.tourismsv.dto.request.ReviewRequest;
import com.tourismsv.dto.response.ReviewResponse;
import com.tourismsv.exception.BusinessException;
import com.tourismsv.exception.ResourceNotFoundException;
import com.tourismsv.repository.DestinationRepository;
import com.tourismsv.repository.DestinationReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final DestinationReviewRepository reviewRepository;
    private final DestinationRepository destinationRepository;

    @Transactional(readOnly = true)
    public Page<ReviewResponse> findByDestinationId(UUID destinationId, Pageable pageable) {
        if (!destinationRepository.existsById(destinationId)) {
            throw new ResourceNotFoundException("Destination", "id", destinationId);
        }
        return reviewRepository.findByDestinationId(destinationId, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public ReviewResponse create(UUID destinationId, User user, ReviewRequest request) {
        if (reviewRepository.existsByDestinationIdAndUserId(destinationId, user.getId())) {
            throw new BusinessException("You have already reviewed this destination");
        }

        var destination = destinationRepository.findById(destinationId)
                .orElseThrow(() -> new ResourceNotFoundException("Destination", "id", destinationId));

        var review = com.tourismsv.domain.entity.DestinationReview.builder()
                .destination(destination)
                .user(user)
                .value(request.value())
                .content(request.content())
                .build();

        return toResponse(reviewRepository.save(review));
    }

    @Transactional
    public ReviewResponse update(UUID destinationId, UUID reviewId, User user, ReviewRequest request) {
        var review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("DestinationReview", "id", reviewId));

        if (!review.getDestination().getId().equals(destinationId)) {
            throw new ResourceNotFoundException("DestinationReview", "id", reviewId);
        }

        if (!review.getUser().getId().equals(user.getId())) {
            throw new BusinessException("You can only edit your own review");
        }

        review.setValue(request.value());
        review.setContent(request.content());

        return toResponse(reviewRepository.save(review));
    }

    @Transactional
    public void delete(UUID destinationId, UUID reviewId, User user) {
        var review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("DestinationReview", "id", reviewId));

        if (!review.getDestination().getId().equals(destinationId)) {
            throw new ResourceNotFoundException("DestinationReview", "id", reviewId);
        }

        var isAdmin = user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!review.getUser().getId().equals(user.getId()) && !isAdmin) {
            throw new BusinessException("You can only delete your own review");
        }

        reviewRepository.delete(review);
    }

    private ReviewResponse toResponse(com.tourismsv.domain.entity.DestinationReview review) {
        return new ReviewResponse(
                review.getId(),
                review.getValue(),
                review.getContent(),
                review.getUser().getId(),
                review.getUser().getName(),
                review.getCreatedAt()
        );
    }
}
