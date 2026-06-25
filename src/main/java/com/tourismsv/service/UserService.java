package com.tourismsv.service;

import com.tourismsv.domain.entity.User;
import com.tourismsv.dto.request.UserUpdateRequest;
import com.tourismsv.dto.response.UserResponse;
import com.tourismsv.exception.BusinessException;
import com.tourismsv.exception.ConflictException;
import com.tourismsv.exception.ResourceNotFoundException;
import com.tourismsv.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<UserResponse> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public UserResponse findById(UUID id) {
        return userRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    @Transactional
    public UserResponse update(UUID id, UserUpdateRequest request, User currentUser) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (!user.getId().equals(currentUser.getId())) {
            throw new BusinessException("You can only edit your own profile");
        }

        if (!user.getEmail().equals(request.email()) && userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already in use: " + request.email());
        }

        user.setName(request.name());
        user.setEmail(request.email());
        user.setUrlAvatar(request.urlAvatar());

        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void updateState(UUID id, com.tourismsv.domain.enums.UserState newState) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        user.setState(newState);
        userRepository.save(user);
    }

    @Transactional
    public void delete(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", "id", id);
        }
        userRepository.deleteById(id);
    }

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getUrlAvatar(),
                user.getRole(),
                user.getState(),
                user.getCreatedAt()
        );
    }
}
