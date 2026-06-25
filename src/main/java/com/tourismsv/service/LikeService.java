package com.tourismsv.service;

import com.tourismsv.domain.entity.Destination;
import com.tourismsv.domain.entity.User;
import com.tourismsv.dto.response.LikeResponse;
import com.tourismsv.exception.ResourceNotFoundException;
import com.tourismsv.repository.DestinationLikeRepository;
import com.tourismsv.repository.DestinationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final DestinationLikeRepository likeRepository;
    private final DestinationRepository destinationRepository;

    @Transactional(readOnly = true)
    public LikeResponse getStatus(UUID destinationId, User user) {
        if (!destinationRepository.existsById(destinationId)) {
            throw new ResourceNotFoundException("Destination", "id", destinationId);
        }

        var liked = user != null && likeRepository.existsByDestinationIdAndUserId(destinationId, user.getId());
        var count = likeRepository.countByDestinationId(destinationId);

        return new LikeResponse(liked, count);
    }

    @Transactional
    public LikeResponse toggle(UUID destinationId, User user) {
        var destination = destinationRepository.findById(destinationId)
                .orElseThrow(() -> new ResourceNotFoundException("Destination", "id", destinationId));

        var existing = likeRepository.findByDestinationIdAndUserId(destinationId, user.getId());

        if (existing.isPresent()) {
            likeRepository.delete(existing.get());
        } else {
            var like = com.tourismsv.domain.entity.DestinationLike.builder()
                    .destination(destination)
                    .user(user)
                    .build();
            likeRepository.save(like);
        }

        var liked = existing.isEmpty();
        var count = likeRepository.countByDestinationId(destinationId);

        return new LikeResponse(liked, count);
    }
}
