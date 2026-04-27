package com.codeit.team4.deokhugam.book.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class BookStatisticsTest {

    @Nested
    @DisplayName("onReviewCreated")
    class OnReviewCreated {

        @Test
        @DisplayName("리뷰 생성 시 reviewCount 증가 및 ratingSum 반영 성공")
        void onReviewCreated_success() {
            BookStatistics statistics = new BookStatistics(UUID.randomUUID());

            statistics.onReviewCreated(4);

            assertThat(statistics.getReviewCount()).isEqualTo(1);
            assertThat(statistics.getRatingSum()).isEqualTo(4);
        }

        @Test
        @DisplayName("리뷰 여러 건 생성 시 누적 성공")
        void onReviewCreated_multiple_success() {
            BookStatistics statistics = new BookStatistics(UUID.randomUUID());

            statistics.onReviewCreated(5);
            statistics.onReviewCreated(3);
            statistics.onReviewCreated(4);

            assertThat(statistics.getReviewCount()).isEqualTo(3);
            assertThat(statistics.getRatingSum()).isEqualTo(12);
        }
    }

    @Nested
    @DisplayName("onReviewDeleted")
    class OnReviewDeleted {

        @Test
        @DisplayName("리뷰 삭제 시 reviewCount 감소 및 ratingSum 반영 성공")
        void onReviewDeleted_success() {
            BookStatistics statistics = new BookStatistics(UUID.randomUUID());
            statistics.onReviewCreated(5);
            statistics.onReviewCreated(3);

            statistics.onReviewDeleted(5);

            assertThat(statistics.getReviewCount()).isEqualTo(1);
            assertThat(statistics.getRatingSum()).isEqualTo(3);
        }

        @Test
        @DisplayName("reviewCount가 0일 때 삭제해도 음수가 되지 않음")
        void onReviewDeleted_zeroCount_success() {
            BookStatistics statistics = new BookStatistics(UUID.randomUUID());

            statistics.onReviewDeleted(5);

            assertThat(statistics.getReviewCount()).isEqualTo(0);
            assertThat(statistics.getRatingSum()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("onReviewUpdated")
    class OnReviewUpdated {

        @Test
        @DisplayName("리뷰 수정 시 ratingSum 변경 성공")
        void onReviewUpdated_success() {
            BookStatistics statistics = new BookStatistics(UUID.randomUUID());
            statistics.onReviewCreated(3);

            statistics.onReviewUpdated(3, 5);

            assertThat(statistics.getReviewCount()).isEqualTo(1);
            assertThat(statistics.getRatingSum()).isEqualTo(5);
        }

        @Test
        @DisplayName("같은 별점으로 수정 시 ratingSum 변경 없음")
        void onReviewUpdated_sameRating_success() {
            BookStatistics statistics = new BookStatistics(UUID.randomUUID());
            statistics.onReviewCreated(4);

            statistics.onReviewUpdated(4, 4);

            assertThat(statistics.getRatingSum()).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("getAverageRating")
    class GetAverageRating {

        @Test
        @DisplayName("리뷰가 없을 때 평균 0 반환 성공")
        void getAverageRating_noReviews_success() {
            BookStatistics statistics = new BookStatistics(UUID.randomUUID());

            assertThat(statistics.getAverageRating()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("평균 별점 소수점 2자리 반올림 성공")
        void getAverageRating_roundHalfUp_success() {
            BookStatistics statistics = new BookStatistics(UUID.randomUUID());
            statistics.onReviewCreated(4);
            statistics.onReviewCreated(3);
            statistics.onReviewCreated(4);

            // (4 + 3 + 4) / 3 = 3.666... → 3.67
            assertThat(statistics.getAverageRating()).isEqualByComparingTo(new BigDecimal("3.67"));
        }

        @Test
        @DisplayName("나누어떨어지는 평균 별점 계산 성공")
        void getAverageRating_exact_success() {
            BookStatistics statistics = new BookStatistics(UUID.randomUUID());
            statistics.onReviewCreated(4);
            statistics.onReviewCreated(4);

            // (4 + 4) / 2 = 4.00
            assertThat(statistics.getAverageRating()).isEqualByComparingTo(new BigDecimal("4.00"));
        }
    }
}