package com.codeit.team4.deokhugam.dashboard.model;

import static com.codeit.team4.deokhugam.jooq.tables.Reviews.REVIEWS;
import static com.codeit.team4.deokhugam.jooq.tables.Users.USERS;

import com.codeit.team4.deokhugam.dashboard.entity.PopularReview;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.impl.DSL;

public record PowerUserSearchModel(
        UUID userId,
        String nickname,
        BigDecimal reviewScoreSum,
        int likeCount,
        int commentCount
) {

    private static final Field<BigDecimal> REVIEW_SCORE_SUM = DSL.sum(
            REVIEWS.LIKE_COUNT.cast(BigDecimal.class).mul(PopularReview.LIKE_COUNT_WEIGHT)
                    .add(REVIEWS.COMMENT_COUNT.cast(BigDecimal.class).mul(PopularReview.COMMENT_COUNT_WEIGHT))
    ).as("review_score_sum");

    private static final Field<BigDecimal> LIKE_COUNT_SUM =
            DSL.sum(REVIEWS.LIKE_COUNT).as("like_count_sum");

    private static final Field<BigDecimal> COMMENT_COUNT_SUM =
            DSL.sum(REVIEWS.COMMENT_COUNT).as("comment_count_sum");

    public static List<Field<?>> toSelectedFields() {
        return List.of(
                REVIEWS.USER_ID,
                USERS.NICKNAME,
                REVIEW_SCORE_SUM,
                LIKE_COUNT_SUM,
                COMMENT_COUNT_SUM
        );
    }

    public static List<Field<?>> toGroupByFields() {
        return List.of(
                REVIEWS.USER_ID,
                USERS.NICKNAME,
                USERS.CREATED_AT
        );
    }

    public static PowerUserSearchModel fromRecord(Record rec) {
        return new PowerUserSearchModel(
                rec.get(REVIEWS.USER_ID),
                rec.get(USERS.NICKNAME),
                rec.get(REVIEW_SCORE_SUM),
                rec.get(LIKE_COUNT_SUM).intValue(),
                rec.get(COMMENT_COUNT_SUM).intValue()
        );
    }
}
