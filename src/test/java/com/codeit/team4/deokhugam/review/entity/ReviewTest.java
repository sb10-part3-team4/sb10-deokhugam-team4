package com.codeit.team4.deokhugam.review.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import com.codeit.team4.deokhugam.book.entity.Book;
import com.codeit.team4.deokhugam.global.error.BusinessException;
import com.codeit.team4.deokhugam.global.error.ErrorCode;
import com.codeit.team4.deokhugam.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ReviewTest {

    private final User user = mock(User.class);
    private final Book book = mock(Book.class);

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5})
    @DisplayName("유효한 평점으로 리뷰 생성 성공")
    void createReview_validRating_success(int rating) {
        Review review = new Review(book, user, "좋은 책입니다", rating);

        assertThat(review.getRating()).isEqualTo(rating);
        assertThat(review.getContent()).isEqualTo("좋은 책입니다");
        assertThat(review.getLikeCount()).isZero();
        assertThat(review.getCommentCount()).isZero();
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 6, 100})
    @DisplayName("평점이 범위를 벗어나면 리뷰 생성 실패")
    void createReview_invalidRating_fail(int rating) {
        assertThatThrownBy(() -> new Review(book, user, "내용", rating))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.INVALID_RATING));
    }
}
