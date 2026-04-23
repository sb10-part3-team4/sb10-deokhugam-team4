package com.codeit.team4.deokhugam.dashboard.builder;

import static com.codeit.team4.deokhugam.jooq.tables.Books.BOOKS;
import static com.codeit.team4.deokhugam.jooq.tables.Reviews.REVIEWS;

import com.codeit.team4.deokhugam.dashboard.entity.PeriodType;
import com.codeit.team4.deokhugam.dashboard.entity.PopularBook;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import org.jooq.Condition;
import org.jooq.SortField;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PopularBookBatchQueryBuilder {

    @Value("${dashboard.batch.zone}")
    private String zone;

    public Condition buildCondition(PeriodType period, LocalDate snapshotDate) {
        Condition condition = REVIEWS.DELETED_AT.isNull()
                .and(BOOKS.DELETED_AT.isNull());

        OffsetDateTime startDateTime = getStartDateTime(period, snapshotDate);
        if (startDateTime != null) {
            condition = condition.and(REVIEWS.CREATED_AT.greaterOrEqual(startDateTime));
        }

        OffsetDateTime endDateTime = snapshotDate.plusDays(1)
                .atStartOfDay(ZoneId.of(zone))
                .toOffsetDateTime();
        condition = condition.and(REVIEWS.CREATED_AT.lessThan(endDateTime));

        return condition;
    }

    public SortField<?> buildOrderBy() {
        return DSL.count().cast(BigDecimal.class)
                .mul(PopularBook.REVIEW_COUNT_WEIGHT)
                .add(DSL.avg(REVIEWS.RATING).cast(BigDecimal.class)
                        .mul(PopularBook.AVG_RATING_WEIGHT))
                .desc();
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
