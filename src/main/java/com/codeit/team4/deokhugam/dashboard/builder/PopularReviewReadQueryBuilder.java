package com.codeit.team4.deokhugam.dashboard.builder;

import static com.codeit.team4.deokhugam.jooq.tables.PopularReviews.POPULAR_REVIEWS;

import com.codeit.team4.deokhugam.dashboard.dto.DashboardSearchRequestParam;
import com.codeit.team4.deokhugam.global.response.SortDirection;
import java.time.LocalDate;
import org.jooq.Condition;
import org.jooq.SortField;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

@Component
public class PopularReviewReadQueryBuilder {

    public Condition buildCondition(DashboardSearchRequestParam param, LocalDate latestSnapshotDate) {
        return DSL.and(
                POPULAR_REVIEWS.PERIOD.eq(param.period().name()),
                POPULAR_REVIEWS.SNAPSHOT_DATE.eq(latestSnapshotDate),
                rankCondition(param)
        );
    }

    public SortField<?> buildOrderBy(SortDirection direction) {
        return SortDirection.ASC == direction
                ? POPULAR_REVIEWS.RANK.asc()
                : POPULAR_REVIEWS.RANK.desc();
    }

    private Condition rankCondition(DashboardSearchRequestParam param) {
        Integer cursor = param.cursorAsInteger();

        if (cursor == null) {
            return DSL.noCondition();
        }

        return switch (param.direction()) {
            case ASC -> POPULAR_REVIEWS.RANK.gt(cursor);
            case DESC -> POPULAR_REVIEWS.RANK.lt(cursor);
        };
    }
}
