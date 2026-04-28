package com.codeit.team4.deokhugam.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.codeit.team4.deokhugam.book.entity.Book;
import com.codeit.team4.deokhugam.book.service.BookService;
import com.codeit.team4.deokhugam.global.error.BusinessException;
import com.codeit.team4.deokhugam.global.error.ErrorCode;
import com.codeit.team4.deokhugam.review.event.ReviewCreatedEvent;
import com.codeit.team4.deokhugam.review.event.ReviewDeletedEvent;
import com.codeit.team4.deokhugam.review.dto.ReviewCreateRequest;
import com.codeit.team4.deokhugam.review.dto.ReviewLikeResponse;
import com.codeit.team4.deokhugam.review.dto.ReviewResponse;
import com.codeit.team4.deokhugam.review.dto.ReviewUpdateRequest;
import com.codeit.team4.deokhugam.review.entity.Review;
import com.codeit.team4.deokhugam.review.mapper.ReviewMapper;
import com.codeit.team4.deokhugam.review.repository.ReviewLikeRepository;
import com.codeit.team4.deokhugam.review.repository.ReviewRepository;
import com.codeit.team4.deokhugam.user.entity.User;
import com.codeit.team4.deokhugam.user.service.UserService;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @InjectMocks
    private ReviewService reviewService;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewLikeRepository reviewLikeRepository;

    @Mock
    private UserService userService;

    @Mock
    private BookService bookService;

    @Mock
    private ReviewMapper reviewMapper;

    @Mock
    ApplicationEventPublisher eventPublisher;

    @Nested
    @DisplayName("리뷰 생성")
    class CreateReview {

        @Test
        @DisplayName("리뷰 생성 성공")
        void createReview_success() {
            UUID userId = UUID.randomUUID();
            UUID bookId = UUID.randomUUID();
            User user = mock(User.class);
            Book book = mock(Book.class);
            ReviewCreateRequest request = new ReviewCreateRequest(bookId, userId, "좋은 책입니다", 5);
            ReviewResponse expectedResponse = new ReviewResponse(
                    UUID.randomUUID(),
                    bookId,
                    "클린 코드",
                    null,
                    userId,
                    "테스터",
                    "좋은 책입니다",
                    5,
                    0,
                    0,
                    false,
                    Instant.now(),
                    Instant.now()
            );

            given(userService.findById(userId)).willReturn(user);
            given(bookService.findById(bookId)).willReturn(book);
            given(book.getId()).willReturn(bookId);
            given(user.getId()).willReturn(userId);
            given(reviewRepository.existsByBookIdAndUserIdAndDeletedAtIsNull(bookId, userId)).willReturn(false);
            given(reviewRepository.save(any(Review.class))).willAnswer(invocation -> invocation.getArgument(0));
            given(reviewMapper.toResponse(any(Review.class), eq(false))).willReturn(expectedResponse);

            ReviewResponse response = reviewService.createReview(request);

            assertThat(response.content()).isEqualTo("좋은 책입니다");
            assertThat(response.rating()).isEqualTo(5);
            verify(reviewRepository).save(any(Review.class));
            verify(eventPublisher).publishEvent(new ReviewCreatedEvent(bookId, 5));
        }

        @Test
        @DisplayName("중복 리뷰 생성 실패")
        void createReview_duplicate_fail() {
            UUID userId = UUID.randomUUID();
            UUID bookId = UUID.randomUUID();
            User user = mock(User.class);
            Book book = mock(Book.class);
            ReviewCreateRequest request = new ReviewCreateRequest(bookId, userId, "좋은 책입니다", 5);

            given(userService.findById(userId)).willReturn(user);
            given(bookService.findById(bookId)).willReturn(book);
            given(book.getId()).willReturn(bookId);
            given(user.getId()).willReturn(userId);
            given(reviewRepository.existsByBookIdAndUserIdAndDeletedAtIsNull(bookId, userId)).willReturn(true);

            assertThatThrownBy(() -> reviewService.createReview(request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.DUPLICATE_REVIEW));
            verify(eventPublisher, never()).publishEvent(any(ReviewCreatedEvent.class));
        }

        @Test
        @DisplayName("존재하지 않는 사용자로 리뷰 생성 실패")
        void createReview_userNotFound_fail() {
            UUID userId = UUID.randomUUID();
            UUID bookId = UUID.randomUUID();
            ReviewCreateRequest request = new ReviewCreateRequest(bookId, userId, "좋은 책입니다", 5);

            given(userService.findById(userId))
                    .willThrow(new BusinessException(ErrorCode.USER_NOT_FOUND));

            assertThatThrownBy(() -> reviewService.createReview(request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.USER_NOT_FOUND));
            verify(eventPublisher, never()).publishEvent(any(ReviewCreatedEvent.class));
        }

        @Test
        @DisplayName("존재하지 않는 도서로 리뷰 생성 실패")
        void createReview_bookNotFound_fail() {
            UUID userId = UUID.randomUUID();
            UUID bookId = UUID.randomUUID();
            User user = mock(User.class);
            ReviewCreateRequest request = new ReviewCreateRequest(bookId, userId, "좋은 책입니다", 5);

            given(userService.findById(userId)).willReturn(user);
            given(bookService.findById(bookId))
                    .willThrow(new BusinessException(ErrorCode.BOOK_NOT_FOUND));

            assertThatThrownBy(() -> reviewService.createReview(request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.BOOK_NOT_FOUND));
            verify(eventPublisher, never()).publishEvent(any(ReviewCreatedEvent.class));
        }

    }

    @Nested
    @DisplayName("리뷰 단건 조회")
    class GetReview {

        @Test
        @DisplayName("리뷰 단건 조회 성공")
        void getReview_success() {
            UUID reviewId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            Review review = mock(Review.class);
            ReviewResponse expectedResponse = new ReviewResponse(
                    reviewId,
                    UUID.randomUUID(),
                    "클린 코드",
                    null,
                    userId,
                    "테스터",
                    "좋은 책입니다",
                    5,
                    0,
                    0,
                    true,
                    Instant.now(),
                    Instant.now()
            );

            given(reviewRepository.findByIdAndDeletedAtIsNull(reviewId)).willReturn(Optional.of(review));
            given(reviewLikeRepository.existsByReviewIdAndUserId(reviewId, userId)).willReturn(true);
            given(reviewMapper.toResponse(review, true)).willReturn(expectedResponse);

            ReviewResponse response = reviewService.getReview(reviewId, userId);

            assertThat(response.content()).isEqualTo("좋은 책입니다");
            assertThat(response.likedByMe()).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 리뷰 단건 조회 실패")
        void getReview_notFound_fail() {
            UUID reviewId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            given(reviewRepository.findByIdAndDeletedAtIsNull(reviewId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> reviewService.getReview(reviewId, userId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.REVIEW_NOT_FOUND));
        }
    }

    @Nested
    @DisplayName("리뷰 조회")
    class FindById {

        @Test
        @DisplayName("리뷰 ID로 조회 성공")
        void findById_success() {
            UUID reviewId = UUID.randomUUID();
            Review review = mock(Review.class);

            given(reviewRepository.findByIdAndDeletedAtIsNull(reviewId)).willReturn(Optional.of(review));

            Review result = reviewService.findById(reviewId);

            assertThat(result).isEqualTo(review);
        }

        @Test
        @DisplayName("존재하지 않는 리뷰 ID로 조회 실패")
        void findById_notFound_fail() {
            UUID reviewId = UUID.randomUUID();

            given(reviewRepository.findByIdAndDeletedAtIsNull(reviewId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> reviewService.findById(reviewId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.REVIEW_NOT_FOUND));
        }
    }

    @Nested
    @DisplayName("리뷰 수정")
    class UpdateReview {

        @Test
        @DisplayName("리뷰 수정 성공")
        void updateReview_success() {
            UUID reviewId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            UUID bookId = UUID.randomUUID();
            Review review = mock(Review.class);
            Book book = mock(Book.class);
            ReviewUpdateRequest request = new ReviewUpdateRequest("수정된 내용", 3);
            ReviewResponse expectedResponse = new ReviewResponse(
                    reviewId,
                    bookId,
                    "클린 코드",
                    null,
                    userId,
                    "테스터",
                    "수정된 내용",
                    3,
                    0,
                    0,
                    true,
                    Instant.now(),
                    Instant.now()
            );

            given(reviewRepository.findByIdAndDeletedAtIsNull(reviewId)).willReturn(Optional.of(review));
            given(review.isOwner(userId)).willReturn(true);
            given(review.getRating()).willReturn(5);
            given(review.getBook()).willReturn(book);
            given(book.getId()).willReturn(bookId);
            given(reviewLikeRepository.existsByReviewIdAndUserId(reviewId, userId)).willReturn(true);
            given(reviewMapper.toResponse(review, true)).willReturn(expectedResponse);

            ReviewResponse response = reviewService.updateReview(reviewId, userId, request);

            assertThat(response.content()).isEqualTo("수정된 내용");
            assertThat(response.rating()).isEqualTo(3);
            assertThat(response.likedByMe()).isTrue();
            verify(review).update("수정된 내용", 3);
        }

        @Test
        @DisplayName("본인의 리뷰가 아니면 수정 실패")
        void updateReview_notOwner_fail() {
            UUID reviewId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            Review review = mock(Review.class);
            ReviewUpdateRequest request = new ReviewUpdateRequest("수정된 내용", 3);

            given(reviewRepository.findByIdAndDeletedAtIsNull(reviewId)).willReturn(Optional.of(review));
            given(review.isOwner(userId)).willReturn(false);
            given(review.getId()).willReturn(reviewId);

            assertThatThrownBy(() -> reviewService.updateReview(reviewId, userId, request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.REVIEW_NOT_OWNER));
        }

        @Test
        @DisplayName("존재하지 않는 리뷰 수정 실패")
        void updateReview_notFound_fail() {
            UUID reviewId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            ReviewUpdateRequest request = new ReviewUpdateRequest("수정된 내용", 3);

            given(reviewRepository.findByIdAndDeletedAtIsNull(reviewId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> reviewService.updateReview(reviewId, userId, request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.REVIEW_NOT_FOUND));
        }
    }

    @Nested
    @DisplayName("리뷰 삭제")
    class SoftDeleteReview {

        @Test
        @DisplayName("리뷰 삭제 성공")
        void softDeleteReview_success() {
            UUID reviewId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            UUID bookId = UUID.randomUUID();
            Review review = mock(Review.class);
            Book book = mock(Book.class);

            given(reviewRepository.findByIdAndDeletedAtIsNull(reviewId)).willReturn(Optional.of(review));
            given(review.isOwner(userId)).willReturn(true);
            given(review.getBook()).willReturn(book);
            given(book.getId()).willReturn(bookId);
            given(review.getRating()).willReturn(4);

            reviewService.softDeleteReview(reviewId, userId);

            verify(review).softDelete();
            verify(eventPublisher).publishEvent(new ReviewDeletedEvent(bookId, 4));
        }

        @Test
        @DisplayName("본인의 리뷰가 아니면 삭제 실패")
        void softDeleteReview_notOwner_fail() {
            UUID reviewId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            Review review = mock(Review.class);

            given(reviewRepository.findByIdAndDeletedAtIsNull(reviewId)).willReturn(Optional.of(review));
            given(review.isOwner(userId)).willReturn(false);
            given(review.getId()).willReturn(reviewId);

            assertThatThrownBy(() -> reviewService.softDeleteReview(reviewId, userId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.REVIEW_NOT_OWNER));
            verify(eventPublisher, never()).publishEvent(any(ReviewDeletedEvent.class));
        }

        @Test
        @DisplayName("존재하지 않는 리뷰 삭제 실패")
        void softDeleteReview_notFound_fail() {
            UUID reviewId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            given(reviewRepository.findByIdAndDeletedAtIsNull(reviewId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> reviewService.softDeleteReview(reviewId, userId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.REVIEW_NOT_FOUND));
            verify(eventPublisher, never()).publishEvent(any(ReviewDeletedEvent.class));
        }
    }

    @Nested
    @DisplayName("리뷰 좋아요 토글")
    class ToggleLike {

        @Test
        @DisplayName("좋아요 추가 성공")
        void toggleLike_like_success() {
            UUID reviewId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            UUID ownerId = UUID.randomUUID();
            Review review = mock(Review.class);
            User user = mock(User.class);
            User reviewOwner = mock(User.class);

            given(reviewRepository.findByIdAndDeletedAtIsNull(reviewId)).willReturn(Optional.of(review));
            given(reviewLikeRepository.existsByReviewIdAndUserId(reviewId, userId)).willReturn(false);
            given(userService.findById(userId)).willReturn(user);
            given(review.getId()).willReturn(reviewId);
            given(review.getUser()).willReturn(reviewOwner);
            given(reviewOwner.getId()).willReturn(ownerId);

            ReviewLikeResponse response = reviewService.toggleLike(reviewId, userId);

            assertThat(response.liked()).isTrue();
            assertThat(response.reviewId()).isEqualTo(reviewId);
            assertThat(response.userId()).isEqualTo(userId);
            verify(reviewLikeRepository).save(any());
            verify(reviewRepository).increaseLikeCount(reviewId);
        }

        @Test
        @DisplayName("좋아요 취소 성공")
        void toggleLike_unlike_success() {
            UUID reviewId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            Review review = mock(Review.class);

            given(reviewRepository.findByIdAndDeletedAtIsNull(reviewId)).willReturn(Optional.of(review));
            given(reviewLikeRepository.existsByReviewIdAndUserId(reviewId, userId)).willReturn(true);
            given(reviewLikeRepository.deleteByReviewIdAndUserId(reviewId, userId)).willReturn(1L);

            ReviewLikeResponse response = reviewService.toggleLike(reviewId, userId);

            assertThat(response.liked()).isFalse();
            assertThat(response.reviewId()).isEqualTo(reviewId);
            assertThat(response.userId()).isEqualTo(userId);
            verify(reviewLikeRepository).deleteByReviewIdAndUserId(reviewId, userId);
            verify(reviewRepository).decreaseLikeCount(reviewId);
        }

        @Test
        @DisplayName("존재하지 않는 리뷰 좋아요 토글 실패")
        void toggleLike_reviewNotFound_fail() {
            UUID reviewId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            given(reviewRepository.findByIdAndDeletedAtIsNull(reviewId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> reviewService.toggleLike(reviewId, userId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.REVIEW_NOT_FOUND));
        }
    }

    @Nested
    @DisplayName("리뷰 물리 삭제")
    class HardDeleteReview {

        @Test
        @DisplayName("논리 삭제되지 않은 리뷰 물리 삭제 성공")
        void hardDeleteReview_notSoftDeleted_success() {
            UUID reviewId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            UUID bookId = UUID.randomUUID();
            Review review = mock(Review.class);
            Book book = mock(Book.class);

            given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
            given(review.isOwner(userId)).willReturn(true);
            given(review.getDeletedAt()).willReturn(null);
            given(review.getBook()).willReturn(book);
            given(book.getId()).willReturn(bookId);
            given(review.getRating()).willReturn(4);

            reviewService.hardDeleteReview(reviewId, userId);

            verify(eventPublisher).publishEvent(new ReviewDeletedEvent(bookId, 4));
            verify(reviewRepository).delete(review);
        }

        @Test
        @DisplayName("이미 논리 삭제된 리뷰 물리 삭제 성공")
        void hardDeleteReview_alreadySoftDeleted_success() {
            UUID reviewId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            Review review = mock(Review.class);

            given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
            given(review.isOwner(userId)).willReturn(true);
            given(review.getDeletedAt()).willReturn(Instant.now());

            reviewService.hardDeleteReview(reviewId, userId);

            verify(eventPublisher, never()).publishEvent(any(ReviewDeletedEvent.class));
            verify(reviewRepository).delete(review);
        }

        @Test
        @DisplayName("본인의 리뷰가 아니면 물리 삭제 실패")
        void hardDeleteReview_notOwner_fail() {
            UUID reviewId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            Review review = mock(Review.class);

            given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
            given(review.isOwner(userId)).willReturn(false);
            given(review.getId()).willReturn(reviewId);

            assertThatThrownBy(() -> reviewService.hardDeleteReview(reviewId, userId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.REVIEW_NOT_OWNER));
            verify(eventPublisher, never()).publishEvent(any(ReviewDeletedEvent.class));
        }

        @Test
        @DisplayName("존재하지 않는 리뷰 물리 삭제 실패")
        void hardDeleteReview_notFound_fail() {
            UUID reviewId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            given(reviewRepository.findById(reviewId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> reviewService.hardDeleteReview(reviewId, userId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.REVIEW_NOT_FOUND));
            verify(eventPublisher, never()).publishEvent(any(ReviewDeletedEvent.class));
        }
    }
}
