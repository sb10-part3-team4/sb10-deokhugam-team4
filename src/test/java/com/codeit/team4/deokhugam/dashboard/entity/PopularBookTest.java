package com.codeit.team4.deokhugam.dashboard.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PopularBookTest {

    @Test
    @DisplayName("score 계산 성공")
    void calculateScore_success() {
        PopularBook popularBook = new PopularBook(
                null,
                "클린 코드",
                "로버트 마틴",
                null,
                PeriodType.DAILY,
                1,
                10,
                new BigDecimal("4.50"),
                LocalDate.of(2026, 4, 22)
        );

        // score = 10 * 0.4 + 4.50 * 0.6 = 4.0 + 2.700 = 6.700
        assertThat(popularBook.getScore()).isEqualByComparingTo(new BigDecimal("6.700"));
    }

    @Test
    @DisplayName("리뷰 0건일 때 score 계산 성공")
    void calculateScore_zeroReview_success() {
        PopularBook popularBook = new PopularBook(
                null,
                "테스트 책",
                "저자",
                null,
                PeriodType.WEEKLY,
                1,
                0,
                new BigDecimal("0.00"),
                LocalDate.of(2026, 4, 22)
        );

        assertThat(popularBook.getScore()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
