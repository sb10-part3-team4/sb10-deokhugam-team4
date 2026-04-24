package com.codeit.team4.deokhugam.dashboard.model;

import static com.codeit.team4.deokhugam.jooq.tables.PowerUsers.POWER_USERS;

import com.codeit.team4.deokhugam.dashboard.entity.PeriodType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.jooq.Field;
import org.jooq.Record;

public record PowerUserViewModel(
        UUID id,
        UUID userId,
        String nickname,
        PeriodType period,
        int rank,
        BigDecimal score,
        BigDecimal reviewScoreSum,
        int likeCount,
        int commentCount,
        Instant createdAt
) implements RankedViewModel {

    public static List<Field<?>> toSelectedFields() {
        return List.of(
                POWER_USERS.ID,
                POWER_USERS.USER_ID,
                POWER_USERS.NICKNAME,
                POWER_USERS.PERIOD,
                POWER_USERS.RANK,
                POWER_USERS.SCORE,
                POWER_USERS.REVIEW_SCORE_SUM,
                POWER_USERS.LIKE_COUNT,
                POWER_USERS.COMMENT_COUNT,
                POWER_USERS.CREATED_AT
        );
    }

    public static PowerUserViewModel fromRecord(Record rec) {
        return new PowerUserViewModel(
                rec.get(POWER_USERS.ID),
                rec.get(POWER_USERS.USER_ID),
                rec.get(POWER_USERS.NICKNAME),
                PeriodType.valueOf(rec.get(POWER_USERS.PERIOD)),
                rec.get(POWER_USERS.RANK),
                rec.get(POWER_USERS.SCORE),
                rec.get(POWER_USERS.REVIEW_SCORE_SUM),
                rec.get(POWER_USERS.LIKE_COUNT),
                rec.get(POWER_USERS.COMMENT_COUNT),
                rec.get(POWER_USERS.CREATED_AT).toInstant()
        );
    }
}
