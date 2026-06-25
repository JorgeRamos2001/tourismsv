package com.tourismsv.service;

import com.tourismsv.domain.entity.Destination;
import com.tourismsv.domain.entity.DestinationReview;
import com.tourismsv.domain.entity.User;
import com.tourismsv.domain.enums.Role;
import com.tourismsv.domain.enums.UserState;
import com.tourismsv.dto.request.ReviewRequest;
import com.tourismsv.dto.response.ReviewResponse;
import com.tourismsv.exception.BusinessException;
import com.tourismsv.exception.ResourceNotFoundException;
import com.tourismsv.repository.DestinationRepository;
import com.tourismsv.repository.DestinationReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private DestinationReviewRepository reviewRepository;

    @Mock
    private DestinationRepository destinationRepository;

    private ReviewService reviewService;

    private static final UUID DESTINATION_ID = UUID.randomUUID();
    private static final UUID REVIEW_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID OTHER_USER_ID = UUID.randomUUID();

    private static final Destination DESTINATION = Destination.builder()
            .id(DESTINATION_ID)
            .build();

    private static final User TOURIST_USER = User.builder()
            .id(USER_ID)
            .name("Tourist")
            .email("tourist@test.com")
            .password("pass")
            .role(Role.TOURIST)
            .state(UserState.ACTIVE)
            .build();

    private static final User ADMIN_USER = User.builder()
            .id(OTHER_USER_ID)
            .name("Admin")
            .email("admin@test.com")
            .password("pass")
            .role(Role.ADMIN)
            .state(UserState.ACTIVE)
            .build();

    private static final DestinationReview REVIEW = DestinationReview.builder()
            .id(REVIEW_ID)
            .destination(DESTINATION)
            .user(TOURIST_USER)
            .value(5)
            .content("Great place!")
            .createdAt(LocalDateTime.now())
            .build();

    private static final ReviewRequest REQUEST = new ReviewRequest(4, "Nice!");

    @BeforeEach
    void setUp() {
        reviewService = new ReviewService(reviewRepository, destinationRepository);
    }

    @Nested
    class FindByDestinationId {

        @Test
        void shouldReturnMappedPage_whenDestinationExists() {
            var pageable = PageRequest.of(0, 10);
            var reviewPage = new PageImpl<>(List.of(REVIEW));
            when(destinationRepository.existsById(DESTINATION_ID)).thenReturn(true);
            when(reviewRepository.findByDestinationId(DESTINATION_ID, pageable)).thenReturn(reviewPage);

            Page<ReviewResponse> result = reviewService.findByDestinationId(DESTINATION_ID, pageable);

            assertThat(result.getContent()).hasSize(1);
            var response = result.getContent().getFirst();
            assertThat(response.id()).isEqualTo(REVIEW_ID);
            assertThat(response.value()).isEqualTo(5);
            assertThat(response.content()).isEqualTo("Great place!");
            assertThat(response.userId()).isEqualTo(USER_ID);
            assertThat(response.userName()).isEqualTo("Tourist");
            assertThat(response.createdAt()).isNotNull();
            verify(destinationRepository).existsById(DESTINATION_ID);
            verify(reviewRepository).findByDestinationId(DESTINATION_ID, pageable);
        }

        @Test
        void shouldThrow_whenDestinationDoesNotExist() {
            var pageable = PageRequest.of(0, 10);
            when(destinationRepository.existsById(DESTINATION_ID)).thenReturn(false);

            assertThatThrownBy(() -> reviewService.findByDestinationId(DESTINATION_ID, pageable))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Destination");

            verify(destinationRepository).existsById(DESTINATION_ID);
            verifyNoMoreInteractions(destinationRepository, reviewRepository);
        }
    }

    @Nested
    class Create {

        @Test
        void shouldSaveAndReturnResponse_whenValid() {
            when(reviewRepository.existsByDestinationIdAndUserId(DESTINATION_ID, USER_ID)).thenReturn(false);
            when(destinationRepository.findById(DESTINATION_ID)).thenReturn(Optional.of(DESTINATION));
            var savedReview = DestinationReview.builder()
                    .id(UUID.randomUUID())
                    .destination(DESTINATION)
                    .user(TOURIST_USER)
                    .value(REQUEST.value())
                    .content(REQUEST.content())
                    .createdAt(LocalDateTime.now())
                    .build();
            when(reviewRepository.save(any(DestinationReview.class))).thenReturn(savedReview);

            ReviewResponse response = reviewService.create(DESTINATION_ID, TOURIST_USER, REQUEST);

            assertThat(response.value()).isEqualTo(REQUEST.value());
            assertThat(response.content()).isEqualTo(REQUEST.content());
            assertThat(response.userId()).isEqualTo(USER_ID);
            assertThat(response.userName()).isEqualTo("Tourist");
            verify(reviewRepository).existsByDestinationIdAndUserId(DESTINATION_ID, USER_ID);
            verify(destinationRepository).findById(DESTINATION_ID);
            verify(reviewRepository).save(any(DestinationReview.class));
        }

        @Test
        void shouldThrow_whenDuplicateReview() {
            when(reviewRepository.existsByDestinationIdAndUserId(DESTINATION_ID, USER_ID)).thenReturn(true);

            assertThatThrownBy(() -> reviewService.create(DESTINATION_ID, TOURIST_USER, REQUEST))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("already reviewed");

            verify(reviewRepository).existsByDestinationIdAndUserId(DESTINATION_ID, USER_ID);
            verifyNoMoreInteractions(reviewRepository, destinationRepository);
        }

        @Test
        void shouldThrow_whenDestinationNotFound() {
            when(reviewRepository.existsByDestinationIdAndUserId(DESTINATION_ID, USER_ID)).thenReturn(false);
            when(destinationRepository.findById(DESTINATION_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reviewService.create(DESTINATION_ID, TOURIST_USER, REQUEST))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Destination");

            verify(reviewRepository).existsByDestinationIdAndUserId(DESTINATION_ID, USER_ID);
            verify(destinationRepository).findById(DESTINATION_ID);
            verifyNoMoreInteractions(reviewRepository);
        }
    }

    @Nested
    class Update {

        @Test
        void shouldUpdateAndReturnResponse_whenValid() {
            when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(REVIEW));
            var savedReview = DestinationReview.builder()
                    .id(REVIEW_ID)
                    .destination(DESTINATION)
                    .user(TOURIST_USER)
                    .value(REQUEST.value())
                    .content(REQUEST.content())
                    .createdAt(REVIEW.getCreatedAt())
                    .build();
            when(reviewRepository.save(REVIEW)).thenReturn(savedReview);

            ReviewResponse response = reviewService.update(DESTINATION_ID, REVIEW_ID, TOURIST_USER, REQUEST);

            assertThat(response.value()).isEqualTo(REQUEST.value());
            assertThat(response.content()).isEqualTo(REQUEST.content());
            assertThat(response.userId()).isEqualTo(USER_ID);
            verify(reviewRepository).findById(REVIEW_ID);
            verify(reviewRepository).save(REVIEW);
        }

        @Test
        void shouldThrow_whenReviewNotFound() {
            when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reviewService.update(DESTINATION_ID, REVIEW_ID, TOURIST_USER, REQUEST))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("DestinationReview");

            verify(reviewRepository).findById(REVIEW_ID);
            verifyNoMoreInteractions(reviewRepository);
        }

        @Test
        void shouldThrow_whenDestinationMismatch() {
            var otherDestination = Destination.builder().id(UUID.randomUUID()).build();
            var reviewOtherDest = DestinationReview.builder()
                    .id(REVIEW_ID)
                    .destination(otherDestination)
                    .user(TOURIST_USER)
                    .build();
            when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(reviewOtherDest));

            assertThatThrownBy(() -> reviewService.update(DESTINATION_ID, REVIEW_ID, TOURIST_USER, REQUEST))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("DestinationReview");

            verify(reviewRepository).findById(REVIEW_ID);
            verifyNoMoreInteractions(reviewRepository);
        }

        @Test
        void shouldThrow_whenNotOwnReview() {
            when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(REVIEW));

            assertThatThrownBy(() -> reviewService.update(DESTINATION_ID, REVIEW_ID, ADMIN_USER, REQUEST))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("own review");

            verify(reviewRepository).findById(REVIEW_ID);
            verifyNoMoreInteractions(reviewRepository);
        }
    }

    @Nested
    class Delete {

        @Test
        void shouldDeleteOwnReview_whenTourist() {
            when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(REVIEW));

            reviewService.delete(DESTINATION_ID, REVIEW_ID, TOURIST_USER);

            verify(reviewRepository).findById(REVIEW_ID);
            verify(reviewRepository).delete(REVIEW);
        }

        @Test
        void shouldDelete_whenAdminDeletingOtherUsersReview() {
            when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(REVIEW));

            reviewService.delete(DESTINATION_ID, REVIEW_ID, ADMIN_USER);

            verify(reviewRepository).findById(REVIEW_ID);
            verify(reviewRepository).delete(REVIEW);
        }

        @Test
        void shouldThrow_whenNotOwnReviewAndNotAdmin() {
            var otherUser = User.builder()
                    .id(UUID.randomUUID())
                    .name("Other")
                    .email("other@test.com")
                    .password("pass")
                    .role(Role.TOURIST)
                    .state(UserState.ACTIVE)
                    .build();
            when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(REVIEW));

            assertThatThrownBy(() -> reviewService.delete(DESTINATION_ID, REVIEW_ID, otherUser))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("own review");

            verify(reviewRepository).findById(REVIEW_ID);
            verifyNoMoreInteractions(reviewRepository);
        }

        @Test
        void shouldThrow_whenReviewNotFound() {
            when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reviewService.delete(DESTINATION_ID, REVIEW_ID, TOURIST_USER))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("DestinationReview");

            verify(reviewRepository).findById(REVIEW_ID);
            verifyNoMoreInteractions(reviewRepository);
        }

        @Test
        void shouldThrow_whenDestinationMismatch() {
            var otherDestination = Destination.builder().id(UUID.randomUUID()).build();
            var reviewOtherDest = DestinationReview.builder()
                    .id(REVIEW_ID)
                    .destination(otherDestination)
                    .user(TOURIST_USER)
                    .build();
            when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(reviewOtherDest));

            assertThatThrownBy(() -> reviewService.delete(DESTINATION_ID, REVIEW_ID, TOURIST_USER))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("DestinationReview");

            verify(reviewRepository).findById(REVIEW_ID);
            verifyNoMoreInteractions(reviewRepository);
        }
    }
}
