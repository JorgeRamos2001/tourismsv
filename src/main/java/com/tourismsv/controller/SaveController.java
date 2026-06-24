package com.tourismsv.controller;

import com.tourismsv.domain.entity.User;
import com.tourismsv.dto.response.DestinationResponse;
import com.tourismsv.dto.response.SaveResponse;
import com.tourismsv.repository.UserRepository;
import com.tourismsv.service.SaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/destinations/{destinationId}/saves")
@RequiredArgsConstructor
public class SaveController {

    private final SaveService saveService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<SaveResponse> getStatus(@PathVariable UUID destinationId,
                                                   Authentication authentication) {
        User user = null;
        if (authentication != null) {
            user = userRepository.findByEmail(authentication.getName()).orElse(null);
        }
        return ResponseEntity.ok(saveService.getStatus(destinationId, user));
    }

    @PostMapping
    public ResponseEntity<SaveResponse> toggle(@PathVariable UUID destinationId,
                                                Authentication authentication) {
        var user = userRepository.findByEmail(authentication.getName())
                .orElseThrow();
        return ResponseEntity.ok(saveService.toggle(destinationId, user));
    }

    @GetMapping("/list")
    public ResponseEntity<Page<DestinationResponse>> listSaved(Authentication authentication,
                                                                Pageable pageable) {
        var user = userRepository.findByEmail(authentication.getName())
                .orElseThrow();
        return ResponseEntity.ok(saveService.findSavedDestinations(user, pageable));
    }
}
