package com.tourismsv.controller;

import com.tourismsv.domain.entity.User;
import com.tourismsv.dto.response.LikeResponse;
import com.tourismsv.repository.UserRepository;
import com.tourismsv.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/destinations/{destinationId}/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<LikeResponse> getStatus(@PathVariable UUID destinationId,
                                                   Authentication authentication) {
        User user = null;
        if (authentication != null) {
            user = userRepository.findByEmail(authentication.getName()).orElse(null);
        }
        return ResponseEntity.ok(likeService.getStatus(destinationId, user));
    }

    @PostMapping
    public ResponseEntity<LikeResponse> toggle(@PathVariable UUID destinationId,
                                                Authentication authentication) {
        var user = userRepository.findByEmail(authentication.getName())
                .orElseThrow();
        return ResponseEntity.ok(likeService.toggle(destinationId, user));
    }
}
