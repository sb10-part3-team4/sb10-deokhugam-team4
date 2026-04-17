package com.codeit.team4.deokhugam.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.codeit.team4.deokhugam.book.entity.Book;
import com.codeit.team4.deokhugam.book.service.BookService;
import com.codeit.team4.deokhugam.global.error.BusinessException;
import com.codeit.team4.deokhugam.global.error.ErrorCode;
import com.codeit.team4.deokhugam.review.dto.ReviewCreateRequest;
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
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @InjectMocks
    private ReviewServiceImpl reviewService;

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
        }

        @Test
        @DisplayName("DB 무결성 위반으로 리뷰 생성 실패")
        void createReview_dataIntegrityViolation_fail() {
            UUID userId = UUID.randomUUID();
            UUID bookId = UUID.randomUUID();
            User user = mock(User.class);
            Book book = mock(Book.class);
            ReviewCreateRequest request = new ReviewCreateRequest(bookId, userId, "좋은 책입니다", 5);

            given(userService.findById(userId)).willReturn(user);
            given(bookService.findById(bookId)).willReturn(book);
            given(book.getId()).willReturn(bookId);
            given(user.getId()).willReturn(userId);
            given(reviewRepository.existsByBookIdAndUserIdAndDeletedAtIsNull(bookId, userId)).willReturn(false);
            given(reviewRepository.save(any(Review.class)))
                    .willThrow(new DataIntegrityViolationException("duplicate key"));

            assertThatThrownBy(() -> reviewService.createReview(request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.DUPLICATE_REVIEW));
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
            User user = mock(User.class);
            Review review = mock(Review.class);
            ReviewUpdateRequest request = new ReviewUpdateRequest("수정된 내용", 3);
            ReviewResponse expectedResponse = new ReviewResponse(
                    reviewId, UUID.randomUUID(), "클린 코드", null,
                    userId, "테스터", "수정된 내용", 3, 0, 0, true,
                    Instant.now(),
                    Instant.now()
            );

            given(userService.findById(userId)).willReturn(user);
            given(reviewRepository.findByIdAndDeletedAtIsNull(reviewId)).willReturn(Optional.of(review));
            given(review.isOwner(user)).willReturn(true);
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
            User user = mock(User.class);
            Review review = mock(Review.class);
            ReviewUpdateRequest request = new ReviewUpdateRequest("수정된 내용", 3);

            given(userService.findById(userId)).willReturn(user);
            given(reviewRepository.findByIdAndDeletedAtIsNull(reviewId)).willReturn(Optional.of(review));
            given(review.isOwner(user)).willReturn(false);
            given(review.getId()).willReturn(reviewId);
            given(user.getId()).willReturn(userId);

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
            User user = mock(User.class);
            ReviewUpdateRequest request = new ReviewUpdateRequest("수정된 내용", 3);

            given(userService.findById(userId)).willReturn(user);
            given(reviewRepository.findByIdAndDeletedAtIsNull(reviewId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> reviewService.updateReview(reviewId, userId, request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.REVIEW_NOT_FOUND));
        }

        @Test
        @DisplayName("존재하지 않는 사용자로 리뷰 수정 실패")
        void updateReview_userNotFound_fail() {
            UUID reviewId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            ReviewUpdateRequest request = new ReviewUpdateRequest("수정된 내용", 3);

            given(userService.findById(userId))
                    .willThrow(new BusinessException(ErrorCode.USER_NOT_FOUND));

            assertThatThrownBy(() -> reviewService.updateReview(reviewId, userId, request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.USER_NOT_FOUND));
        }
    }
}
