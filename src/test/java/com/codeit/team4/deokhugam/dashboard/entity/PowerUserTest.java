package com.codeit.team4.deokhugam.dashboard.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PowerUserTest {

    @Test
    @DisplayName("score 계산 성공")
    void calculateScore_success() {
        PowerUser powerUser = new PowerUser(
                null, "테스터",
                PeriodType.DAILY, 1,
                new BigDecimal("6.5"),
                10, 5,
                LocalDate.of(2026, 4, 23)
        );

        // score = 6.5 * 0.5 + 10 * 0.2 + 5 * 0.3 = 3.25 + 2.0 + 1.5 = 6.75
        assertThat(powerUser.getScore()).isEqualByComparingTo(new BigDecimal("6.75"));
    }

    @Test
    @DisplayName("활동 없을 때 score 계산 성공")
    void calculateScore_zero_success() {
        PowerUser powerUser = new PowerUser(
                null, "테스터",
                PeriodType.WEEKLY, 1,
                BigDecimal.ZERO,
                0, 0,
                LocalDate.of(2026, 4, 23)
        );

        assertThat(powerUser.getScore()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
