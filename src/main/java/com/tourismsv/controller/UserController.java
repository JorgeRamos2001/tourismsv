package com.tourismsv.controller;

import com.tourismsv.domain.entity.User;
import com.tourismsv.dto.request.UserStateUpdateRequest;
import com.tourismsv.dto.request.UserUpdateRequest;
import com.tourismsv.dto.response.UserResponse;
import com.tourismsv.repository.UserRepository;
import com.tourismsv.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponse>> findAll(@ParameterObject @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(userService.findAll(pageable));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(Authentication authentication) {
        var user = userRepository.findByEmail(authentication.getName()).orElseThrow();
        return ResponseEntity.ok(userService.toResponse(user));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable UUID id,
                                                @Valid @RequestBody UserUpdateRequest request,
                                                Authentication authentication) {
        var user = userRepository.findByEmail(authentication.getName()).orElseThrow();
        return ResponseEntity.ok(userService.update(id, request, user));
    }

    @PatchMapping("/{id}/state")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateState(@PathVariable UUID id,
                                             @Valid @RequestBody UserStateUpdateRequest request) {
        userService.updateState(id, request.state());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
