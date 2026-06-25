package com.tourismsv.service;

import com.tourismsv.domain.entity.Destination;
import com.tourismsv.domain.entity.DestinationLike;
import com.tourismsv.domain.entity.User;
import com.tourismsv.dto.response.LikeResponse;
import com.tourismsv.exception.ResourceNotFoundException;
import com.tourismsv.repository.DestinationLikeRepository;
import com.tourismsv.repository.DestinationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @Mock
    private DestinationLikeRepository likeRepository;

    @Mock
    private DestinationRepository destinationRepository;

    private LikeService likeService;

    private static final User TEST_USER = User.builder()
            .id(UUID.randomUUID())
            .email("test@test.com")
            .build();

    private static final UUID DESTINATION_ID = UUID.randomUUID();

    private static final Destination DESTINATION = Destination.builder()
            .id(DESTINATION_ID)
            .build();

    @BeforeEach
    void setUp() {
        likeService = new LikeService(likeRepository, destinationRepository);
    }

    @Nested
    class GetStatus {

        @Test
        void shouldThrowResourceNotFoundException_whenDestinationDoesNotExist() {
            when(destinationRepository.existsById(DESTINATION_ID)).thenReturn(false);

            assertThatThrownBy(() -> likeService.getStatus(DESTINATION_ID, TEST_USER))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Destination");

            verify(destinationRepository).existsById(DESTINATION_ID);
            verifyNoMoreInteractions(destinationRepository, likeRepository);
        }

        @Test
        void shouldReturnLikedTrue_whenUserIsAuthenticatedAndLikeExists() {
            when(destinationRepository.existsById(DESTINATION_ID)).thenReturn(true);
            when(likeRepository.existsByDestinationIdAndUserId(DESTINATION_ID, TEST_USER.getId())).thenReturn(true);
            when(likeRepository.countByDestinationId(DESTINATION_ID)).thenReturn(5L);

            var response = likeService.getStatus(DESTINATION_ID, TEST_USER);

            assertThat(response.liked()).isTrue();
            assertThat(response.likesCount()).isEqualTo(5L);

            verify(likeRepository).existsByDestinationIdAndUserId(DESTINATION_ID, TEST_USER.getId());
            verify(likeRepository).countByDestinationId(DESTINATION_ID);
        }

        @Test
        void shouldReturnLikedFalse_whenUserIsAuthenticatedAndLikeDoesNotExist() {
            when(destinationRepository.existsById(DESTINATION_ID)).thenReturn(true);
            when(likeRepository.existsByDestinationIdAndUserId(DESTINATION_ID, TEST_USER.getId())).thenReturn(false);
            when(likeRepository.countByDestinationId(DESTINATION_ID)).thenReturn(3L);

            var response = likeService.getStatus(DESTINATION_ID, TEST_USER);

            assertThat(response.liked()).isFalse();
            assertThat(response.likesCount()).isEqualTo(3L);

            verify(likeRepository).existsByDestinationIdAndUserId(DESTINATION_ID, TEST_USER.getId());
            verify(likeRepository).countByDestinationId(DESTINATION_ID);
        }

        @Test
        void shouldReturnLikedFalse_whenUserIsAnonymous() {
            when(destinationRepository.existsById(DESTINATION_ID)).thenReturn(true);
            when(likeRepository.countByDestinationId(DESTINATION_ID)).thenReturn(7L);

            var response = likeService.getStatus(DESTINATION_ID, null);

            assertThat(response.liked()).isFalse();
            assertThat(response.likesCount()).isEqualTo(7L);

            verify(likeRepository).countByDestinationId(DESTINATION_ID);
            verifyNoMoreInteractions(likeRepository);
        }
    }

    @Nested
    class Toggle {

        @Test
        void shouldThrowResourceNotFoundException_whenDestinationDoesNotExist() {
            when(destinationRepository.findById(DESTINATION_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> likeService.toggle(DESTINATION_ID, TEST_USER))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Destination");

            verify(destinationRepository).findById(DESTINATION_ID);
            verifyNoMoreInteractions(destinationRepository, likeRepository);
        }

        @Test
        void shouldAddLikeAndReturnLikedTrue_whenNotAlreadyLiked() {
            when(destinationRepository.findById(DESTINATION_ID)).thenReturn(Optional.of(DESTINATION));
            when(likeRepository.findByDestinationIdAndUserId(DESTINATION_ID, TEST_USER.getId())).thenReturn(Optional.empty());
            when(likeRepository.countByDestinationId(DESTINATION_ID)).thenReturn(6L);

            var response = likeService.toggle(DESTINATION_ID, TEST_USER);

            assertThat(response.liked()).isTrue();
            assertThat(response.likesCount()).isEqualTo(6L);

            verify(likeRepository).findByDestinationIdAndUserId(DESTINATION_ID, TEST_USER.getId());
            verify(likeRepository).save(any(DestinationLike.class));
            verify(likeRepository).countByDestinationId(DESTINATION_ID);
        }

        @Test
        void shouldRemoveLikeAndReturnLikedFalse_whenAlreadyLiked() {
            var existingLike = DestinationLike.builder()
                    .id(UUID.randomUUID())
                    .destination(DESTINATION)
                    .user(TEST_USER)
                    .build();

            when(destinationRepository.findById(DESTINATION_ID)).thenReturn(Optional.of(DESTINATION));
            when(likeRepository.findByDestinationIdAndUserId(DESTINATION_ID, TEST_USER.getId())).thenReturn(Optional.of(existingLike));
            when(likeRepository.countByDestinationId(DESTINATION_ID)).thenReturn(4L);

            var response = likeService.toggle(DESTINATION_ID, TEST_USER);

            assertThat(response.liked()).isFalse();
            assertThat(response.likesCount()).isEqualTo(4L);

            verify(likeRepository).findByDestinationIdAndUserId(DESTINATION_ID, TEST_USER.getId());
            verify(likeRepository).delete(existingLike);
            verify(likeRepository).countByDestinationId(DESTINATION_ID);
        }
    }
}
