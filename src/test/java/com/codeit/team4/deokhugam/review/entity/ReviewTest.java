package com.codeit.team4.deokhugam.review.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.codeit.team4.deokhugam.book.entity.Book;
import com.codeit.team4.deokhugam.global.error.BusinessException;
import com.codeit.team4.deokhugam.global.error.ErrorCode;
import com.codeit.team4.deokhugam.user.entity.User;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

    @Nested
    @DisplayName("리뷰 수정")
    class Update {

        @Test
        @DisplayName("리뷰 수정 성공")
        void update_success() {
            Review review = new Review(book, user, "좋은 책입니다", 5);

            review.update("수정된 내용", 3);

            assertThat(review.getContent()).isEqualTo("수정된 내용");
            assertThat(review.getRating()).isEqualTo(3);
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, 0, 6, 100})
        @DisplayName("평점이 범위를 벗어나면 리뷰 수정 실패")
        void update_invalidRating_fail(int rating) {
            Review review = new Review(book, user, "좋은 책입니다", 5);

            assertThatThrownBy(() -> review.update("수정된 내용", rating))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.INVALID_RATING));
        }
    }

    @Nested
    @DisplayName("리뷰 소프트 삭제")
    class SoftDelete {

        @Test
        @DisplayName("소프트 삭제 성공")
        void softDelete_success() {
            Review review = new Review(book, user, "좋은 책입니다", 5);

            review.softDelete();

            assertThat(review.getDeletedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("리뷰 작성자 확인")
    class IsOwner {

        @Test
        @DisplayName("작성자 본인이면 true 반환 성공")
        void isOwner_true() {
            UUID userId = UUID.randomUUID();
            given(user.getId()).willReturn(userId);
            Review review = new Review(book, user, "좋은 책입니다", 5);

            assertThat(review.isOwner(userId)).isTrue();
        }

        @Test
        @DisplayName("작성자가 아니면 false 반환 성공")
        void isOwner_false() {
            given(user.getId()).willReturn(UUID.randomUUID());
            Review review = new Review(book, user, "좋은 책입니다", 5);

            assertThat(review.isOwner(UUID.randomUUID())).isFalse();
        }
    }
}
