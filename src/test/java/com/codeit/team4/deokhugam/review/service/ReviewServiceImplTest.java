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
import com.codeit.team4.deokhugam.review.entity.Review;
import com.codeit.team4.deokhugam.review.mapper.ReviewMapper;
import com.codeit.team4.deokhugam.review.repository.ReviewRepository;
import com.codeit.team4.deokhugam.user.entity.User;
import com.codeit.team4.deokhugam.user.service.UserService;
import org.springframework.dao.DataIntegrityViolationException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @InjectMocks
    private ReviewServiceImpl reviewService;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserService userService;

    @Mock
    private BookService bookService;

    @Mock
    private ReviewMapper reviewMapper;

    @Test
    @DisplayName("리뷰 생성 성공")
    void test1() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        User user = mock(User.class);
        Book book = mock(Book.class);
        ReviewCreateRequest request = new ReviewCreateRequest(bookId, userId, "좋은 책입니다", 5);
        ReviewResponse expectedResponse = new ReviewResponse(
                UUID.randomUUID(), bookId, "클린 코드", null,
                userId, "테스터", "좋은 책입니다", 5, 0, 0, false,
                Instant.now(), Instant.now());

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
    void test2() {
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
    @DisplayName("리뷰 ID로 조회 성공")
    void test3() {
        UUID reviewId = UUID.randomUUID();
        Review review = mock(Review.class);

        given(reviewRepository.findByIdAndDeletedAtIsNull(reviewId)).willReturn(Optional.of(review));

        Review result = reviewService.findById(reviewId);

        assertThat(result).isEqualTo(review);
    }

    @Test
    @DisplayName("존재하지 않는 리뷰 ID로 조회 실패")
    void test4() {
        UUID reviewId = UUID.randomUUID();

        given(reviewRepository.findByIdAndDeletedAtIsNull(reviewId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.findById(reviewId))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.REVIEW_NOT_FOUND));
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 리뷰 생성 실패")
    void test5() {
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
    void test6() {
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
    void test7() {
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
