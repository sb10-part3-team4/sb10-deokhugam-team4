package com.codeit.team4.deokhugam.dashboard.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PopularReviewTest {

    @Test
    @DisplayName("score 계산 성공")
    void calculateScore_success() {
        PopularReview popularReview = new PopularReview(
                null, null, null,
                "책 제목", null, "닉네임",
                "리뷰 내용", 5,
                PeriodType.DAILY, 1,
                10, 5,
                LocalDate.of(2026, 4, 23)
        );

        // score = 10 * 0.3 + 5 * 0.7 = 3.0 + 3.5 = 6.5
        assertThat(popularReview.getScore()).isEqualByComparingTo(new BigDecimal("6.5"));
    }

    @Test
    @DisplayName("좋아요/댓글 0건일 때 score 계산 성공")
    void calculateScore_zero_success() {
        PopularReview popularReview = new PopularReview(
                null, null, null,
                "책 제목", null, "닉네임",
                "리뷰 내용", 3,
                PeriodType.WEEKLY, 1,
                0, 0,
                LocalDate.of(2026, 4, 23)
        );

        assertThat(popularReview.getScore()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
