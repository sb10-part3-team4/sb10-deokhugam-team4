package com.codeit.team4.deokhugam.dashboard.builder;

import static com.codeit.team4.deokhugam.jooq.tables.Comments.COMMENTS;
import static com.codeit.team4.deokhugam.jooq.tables.ReviewLikes.REVIEW_LIKES;
import static com.codeit.team4.deokhugam.jooq.tables.Reviews.REVIEWS;
import static com.codeit.team4.deokhugam.jooq.tables.Users.USERS;

import com.codeit.team4.deokhugam.dashboard.entity.PeriodType;
import com.codeit.team4.deokhugam.dashboard.entity.PopularReview;
import com.codeit.team4.deokhugam.dashboard.entity.PowerUser;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import org.jooq.Condition;
import org.jooq.Field;
import java.util.UUID;
import org.jooq.Record2;
import org.jooq.SortField;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PowerUserAggregationQueryBuilder {

    @Value("${dashboard.batch.zone}")
    private String zone;

    public Condition buildCondition(PeriodType period, LocalDate snapshotDate) {
        return DSL.and(
                REVIEWS.DELETED_AT.isNull(),
                startDateCondition(period, snapshotDate),
                endDateCondition(snapshotDate)
        );
    }

    public List<SortField<?>> buildOrderBy(
            Field<Integer> periodLikeCount,
            Field<Integer> periodCommentCount
    ) {
        SortField<?> scoreDesc = DSL.sum(
                periodLikeCount.cast(BigDecimal.class).mul(PopularReview.LIKE_COUNT_WEIGHT)
                        .add(periodCommentCount.cast(BigDecimal.class).mul(PopularReview.COMMENT_COUNT_WEIGHT))
        ).cast(BigDecimal.class).mul(PowerUser.REVIEW_SCORE_SUM_WEIGHT)
                .add(DSL.sum(periodLikeCount).cast(BigDecimal.class).mul(PowerUser.LIKE_COUNT_WEIGHT))
                .add(DSL.sum(periodCommentCount).cast(BigDecimal.class).mul(PowerUser.COMMENT_COUNT_WEIGHT))
                .desc();

        return List.of(scoreDesc, USERS.CREATED_AT.asc(), REVIEWS.USER_ID.asc());
    }

    public Table<Record2<UUID, Integer>> buildPeriodLikeCountTable(
            PeriodType period,
            LocalDate snapshotDate
    ) {
        Condition condition = REVIEW_LIKES.CREATED_AT.isNotNull();
        OffsetDateTime startDateTime = getStartDateTime(period, snapshotDate);
        OffsetDateTime endDateTime = getEndDateTime(snapshotDate);

        if (startDateTime != null) {
            condition = condition.and(REVIEW_LIKES.CREATED_AT.greaterOrEqual(startDateTime));
        }
        condition = condition.and(REVIEW_LIKES.CREATED_AT.lessThan(endDateTime));

        return DSL.select(
                        REVIEW_LIKES.REVIEW_ID.as("review_id"),
                        DSL.count().as("like_count")
                )
                .from(REVIEW_LIKES)
                .where(condition)
                .groupBy(REVIEW_LIKES.REVIEW_ID)
                .asTable("period_likes");
    }

    public Table<Record2<UUID, Integer>> buildPeriodCommentCountTable(
            PeriodType period,
            LocalDate snapshotDate
    ) {
        Condition condition = DSL.and(
                COMMENTS.DELETED_AT.isNull(),
                COMMENTS.CREATED_AT.isNotNull()
        );
        OffsetDateTime startDateTime = getStartDateTime(period, snapshotDate);
        OffsetDateTime endDateTime = getEndDateTime(snapshotDate);

        if (startDateTime != null) {
            condition = condition.and(COMMENTS.CREATED_AT.greaterOrEqual(startDateTime));
        }
        condition = condition.and(COMMENTS.CREATED_AT.lessThan(endDateTime));

        return DSL.select(
                        COMMENTS.REVIEW_ID.as("review_id"),
                        DSL.count().as("comment_count")
                )
                .from(COMMENTS)
                .where(condition)
                .groupBy(COMMENTS.REVIEW_ID)
                .asTable("period_comments");
    }

    private Condition startDateCondition(PeriodType period, LocalDate snapshotDate) {
        OffsetDateTime startDateTime = getStartDateTime(period, snapshotDate);
        if (startDateTime == null) {
            return DSL.noCondition();
        }
        return REVIEWS.CREATED_AT.greaterOrEqual(startDateTime);
    }

    private Condition endDateCondition(LocalDate snapshotDate) {
        return REVIEWS.CREATED_AT.lessThan(getEndDateTime(snapshotDate));
    }

    private OffsetDateTime getEndDateTime(LocalDate snapshotDate) {
        return snapshotDate.plusDays(1)
                .atStartOfDay(ZoneId.of(zone))
                .toOffsetDateTime();
    }

    private OffsetDateTime getStartDateTime(PeriodType period, LocalDate snapshotDate) {
        LocalDate startDate = switch (period) {
            case DAILY -> snapshotDate;
            case WEEKLY -> snapshotDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case MONTHLY -> snapshotDate.withDayOfMonth(1);
            case ALL_TIME -> null;
        };

        if (startDate == null) {
            return null;
        }

        return startDate.atStartOfDay(ZoneId.of(zone)).toOffsetDateTime();
    }
}
