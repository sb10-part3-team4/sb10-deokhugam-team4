package com.codeit.team4.deokhugam.dashboard.builder;

import static com.codeit.team4.deokhugam.jooq.tables.Books.BOOKS;
import static com.codeit.team4.deokhugam.jooq.tables.Reviews.REVIEWS;

import com.codeit.team4.deokhugam.dashboard.entity.PeriodType;
import com.codeit.team4.deokhugam.dashboard.entity.PopularReview;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import org.jooq.Condition;
import org.jooq.SortField;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PopularReviewAggregationQueryBuilder {

    @Value("${dashboard.batch.zone}")
    private String zone;

    public Condition buildCondition(PeriodType period, LocalDate snapshotDate) {
        return DSL.and(
                REVIEWS.DELETED_AT.isNull(),
                BOOKS.DELETED_AT.isNull(),
                startDateCondition(period, snapshotDate),
                endDateCondition(snapshotDate)
        );
    }

    public List<SortField<?>> buildOrderBy() {
        SortField<?> scoreDesc = REVIEWS.LIKE_COUNT.cast(BigDecimal.class)
                .mul(PopularReview.LIKE_COUNT_WEIGHT)
                .add(REVIEWS.COMMENT_COUNT.cast(BigDecimal.class)
                        .mul(PopularReview.COMMENT_COUNT_WEIGHT))
                .desc();

        return List.of(scoreDesc, REVIEWS.CREATED_AT.asc());
    }

    private Condition startDateCondition(PeriodType period, LocalDate snapshotDate) {
        OffsetDateTime startDateTime = getStartDateTime(period, snapshotDate);
        if (startDateTime == null) {
            return DSL.noCondition();
        }
        return REVIEWS.CREATED_AT.greaterOrEqual(startDateTime);
    }

    private Condition endDateCondition(LocalDate snapshotDate) {
        OffsetDateTime endDateTime = snapshotDate.plusDays(1)
                .atStartOfDay(ZoneId.of(zone))
                .toOffsetDateTime();
        return REVIEWS.CREATED_AT.lessThan(endDateTime);
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
