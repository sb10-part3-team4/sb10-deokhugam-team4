package com.codeit.team4.deokhugam.dashboard.builder;

import static com.codeit.team4.deokhugam.jooq.tables.PopularBooks.POPULAR_BOOKS;

import com.codeit.team4.deokhugam.dashboard.dto.PopularBookSearchRequestParam;
import com.codeit.team4.deokhugam.global.response.SortDirection;
import java.time.LocalDate;
import org.jooq.Condition;
import org.jooq.SortField;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

@Component
public class PopularBookReadQueryBuilder {

    public Condition buildCondition(PopularBookSearchRequestParam param, LocalDate latestSnapshotDate) {
        return DSL.and(
                POPULAR_BOOKS.PERIOD.eq(param.period().name()),
                POPULAR_BOOKS.SNAPSHOT_DATE.eq(latestSnapshotDate),
                rankCondition(param)
        );
    }

    public SortField<?> buildOrderBy(SortDirection direction) {
        return SortDirection.ASC == direction
                ? POPULAR_BOOKS.RANK.asc()
                : POPULAR_BOOKS.RANK.desc();
    }

    private Condition rankCondition(PopularBookSearchRequestParam param) {
        Integer cursor = param.cursorAsInteger();

        if (cursor == null) {
            return DSL.noCondition();
        }

        return switch (param.direction()) {
            case ASC -> POPULAR_BOOKS.RANK.gt(cursor);
            case DESC -> POPULAR_BOOKS.RANK.lt(cursor);
        };
    }
}
