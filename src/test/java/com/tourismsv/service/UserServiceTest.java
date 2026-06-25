package com.tourismsv.service;

import com.tourismsv.domain.entity.User;
import com.tourismsv.domain.enums.Role;
import com.tourismsv.domain.enums.UserState;
import com.tourismsv.dto.request.UserUpdateRequest;
import com.tourismsv.dto.response.UserResponse;
import com.tourismsv.exception.BusinessException;
import com.tourismsv.exception.ConflictException;
import com.tourismsv.exception.ResourceNotFoundException;
import com.tourismsv.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
    }

    @Test
    void findAll_shouldReturnPopulatedPage() {
        var pageable = PageRequest.of(0, 10);
        var now = LocalDateTime.now();
        var user1 = User.builder()
                .id(UUID.randomUUID())
                .name("Alice")
                .email("alice@test.com")
                .urlAvatar("https://avatar.com/alice")
                .role(Role.TOURIST)
                .state(UserState.ACTIVE)
                .createdAt(now)
                .build();
        var user2 = User.builder()
                .id(UUID.randomUUID())
                .name("Bob")
                .email("bob@test.com")
                .urlAvatar("https://avatar.com/bob")
                .role(Role.ADMIN)
                .state(UserState.ACTIVE)
                .createdAt(now)
                .build();
        var page = new PageImpl<User>(List.of(user1, user2), pageable, 2);

        when(userRepository.findAll(pageable)).thenReturn(page);

        var result = userService.findAll(pageable);

        assertEquals(2, result.getContent().size());
        assertEquals(user1.getId(), result.getContent().get(0).id());
        assertEquals("Alice", result.getContent().get(0).name());
        assertEquals("alice@test.com", result.getContent().get(0).email());
        assertEquals(user2.getId(), result.getContent().get(1).id());
        assertEquals("Bob", result.getContent().get(1).name());
    }

    @Test
    void findAll_shouldReturnEmptyPage() {
        var pageable = PageRequest.of(0, 10);
        var page = new PageImpl<User>(List.of(), pageable, 0);

        when(userRepository.findAll(pageable)).thenReturn(page);

        var result = userService.findAll(pageable);

        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void findById_shouldReturnResponseWhenFound() {
        var id = UUID.randomUUID();
        var now = LocalDateTime.now();
        var user = User.builder()
                .id(id)
                .name("Alice")
                .email("alice@test.com")
                .urlAvatar("https://avatar.com/alice")
                .role(Role.TOURIST)
                .state(UserState.ACTIVE)
                .createdAt(now)
                .build();

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        var result = userService.findById(id);

        assertEquals(id, result.id());
        assertEquals("Alice", result.name());
        assertEquals("alice@test.com", result.email());
        assertEquals(Role.TOURIST, result.role());
        assertEquals(UserState.ACTIVE, result.state());
        assertEquals(now, result.createdAt());
    }

    @Test
    void findById_shouldThrowResourceNotFoundExceptionWhenNotFound() {
        var id = UUID.randomUUID();

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        var ex = assertThrows(ResourceNotFoundException.class, () -> userService.findById(id));

        assertTrue(ex.getMessage().contains("User"));
        assertTrue(ex.getMessage().contains(id.toString()));
    }

    @Test
    void update_shouldSucceedWhenEmailUnchanged() {
        var id = UUID.randomUUID();
        var now = LocalDateTime.now();
        var user = User.builder()
                .id(id)
                .name("Alice")
                .email("alice@test.com")
                .urlAvatar("https://avatar.com/alice")
                .role(Role.TOURIST)
                .state(UserState.ACTIVE)
                .createdAt(now)
                .build();
        var currentUser = User.builder().id(id).build();
        var request = new UserUpdateRequest("Alice Updated", "alice@test.com", "https://avatar.com/new");

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = userService.update(id, request, currentUser);

        assertEquals("Alice Updated", result.name());
        assertEquals("alice@test.com", result.email());
        assertEquals("https://avatar.com/new", result.urlAvatar());

        verify(userRepository, never()).existsByEmail(any());
        verify(userRepository).save(user);
    }

    @Test
    void update_shouldSucceedWhenEmailChanged() {
        var id = UUID.randomUUID();
        var now = LocalDateTime.now();
        var user = User.builder()
                .id(id)
                .name("Alice")
                .email("alice@test.com")
                .urlAvatar("https://avatar.com/alice")
                .role(Role.TOURIST)
                .state(UserState.ACTIVE)
                .createdAt(now)
                .build();
        var currentUser = User.builder().id(id).build();
        var request = new UserUpdateRequest("Alice Updated", "alice.new@test.com", "https://avatar.com/new");

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("alice.new@test.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = userService.update(id, request, currentUser);

        assertEquals("alice.new@test.com", result.email());
        verify(userRepository).existsByEmail("alice.new@test.com");
        verify(userRepository).save(user);
    }

    @Test
    void update_shouldThrowResourceNotFoundExceptionWhenNotFound() {
        var id = UUID.randomUUID();
        var currentUser = User.builder().id(UUID.randomUUID()).build();
        var request = new UserUpdateRequest("Alice", "alice@test.com", null);

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        var ex = assertThrows(ResourceNotFoundException.class, () -> userService.update(id, request, currentUser));

        assertTrue(ex.getMessage().contains("User"));
        assertTrue(ex.getMessage().contains(id.toString()));
        verify(userRepository, never()).existsByEmail(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowBusinessExceptionWhenNotOwnProfile() {
        var id = UUID.randomUUID();
        var otherId = UUID.randomUUID();
        var user = User.builder().id(id).name("Alice").email("alice@test.com").build();
        var currentUser = User.builder().id(otherId).build();
        var request = new UserUpdateRequest("Alice", "alice@test.com", null);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        var ex = assertThrows(BusinessException.class, () -> userService.update(id, request, currentUser));

        assertEquals("You can only edit your own profile", ex.getMessage());
        verify(userRepository, never()).existsByEmail(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowConflictExceptionWhenEmailAlreadyInUse() {
        var id = UUID.randomUUID();
        var user = User.builder().id(id).name("Alice").email("alice@test.com").build();
        var currentUser = User.builder().id(id).build();
        var request = new UserUpdateRequest("Alice", "bob@test.com", null);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("bob@test.com")).thenReturn(true);

        var ex = assertThrows(ConflictException.class, () -> userService.update(id, request, currentUser));

        assertTrue(ex.getMessage().contains("bob@test.com"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateState_shouldUpdateStateWhenUserFound() {
        var id = UUID.randomUUID();
        var user = User.builder()
                .id(id)
                .name("Alice")
                .email("alice@test.com")
                .state(UserState.ACTIVE)
                .build();

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.updateState(id, UserState.INACTIVE);

        assertEquals(UserState.INACTIVE, user.getState());
        verify(userRepository).save(user);
    }

    @Test
    void updateState_shouldThrowResourceNotFoundExceptionWhenNotFound() {
        var id = UUID.randomUUID();

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        var ex = assertThrows(ResourceNotFoundException.class, () -> userService.updateState(id, UserState.INACTIVE));

        assertTrue(ex.getMessage().contains("User"));
        assertTrue(ex.getMessage().contains(id.toString()));
        verify(userRepository, never()).save(any());
    }

    @Test
    void delete_shouldDeleteWhenUserExists() {
        var id = UUID.randomUUID();

        when(userRepository.existsById(id)).thenReturn(true);

        userService.delete(id);

        verify(userRepository).deleteById(id);
    }

    @Test
    void delete_shouldThrowResourceNotFoundExceptionWhenNotFound() {
        var id = UUID.randomUUID();

        when(userRepository.existsById(id)).thenReturn(false);

        var ex = assertThrows(ResourceNotFoundException.class, () -> userService.delete(id));

        assertTrue(ex.getMessage().contains("User"));
        assertTrue(ex.getMessage().contains(id.toString()));
        verify(userRepository, never()).deleteById(any());
    }
}
