package com.codeit.team4.deokhugam.dashboard.builder;

import static com.codeit.team4.deokhugam.jooq.tables.PopularBooks.POPULAR_BOOKS;

import com.codeit.team4.deokhugam.dashboard.dto.PopularBookSearchRequestParam;
import com.codeit.team4.deokhugam.global.error.BusinessException;
import com.codeit.team4.deokhugam.global.error.ErrorCode;
import com.codeit.team4.deokhugam.global.response.SortDirection;
import java.time.LocalDate;
import org.jooq.Condition;
import org.jooq.SortField;
import org.springframework.stereotype.Component;

@Component
public class PopularBookReadQueryBuilder {

    public Condition buildCondition(PopularBookSearchRequestParam param, LocalDate latestSnapshotDate) {
        Condition condition = POPULAR_BOOKS.PERIOD.eq(param.period().name())
                .and(POPULAR_BOOKS.SNAPSHOT_DATE.eq(latestSnapshotDate));

        if (param.cursor() != null) {
            boolean isAsc = SortDirection.ASC == param.direction();
            int cursorValue;
            try {
                cursorValue = Integer.parseInt(param.cursor());
            } catch (NumberFormatException e) {
                throw new BusinessException(ErrorCode.INVALID_INPUT, "cursor는 정수여야 합니다");
            }
            condition = isAsc
                    ? condition.and(POPULAR_BOOKS.RANK.gt(cursorValue))
                    : condition.and(POPULAR_BOOKS.RANK.lt(cursorValue));
        }

        return condition;
    }

    public SortField<?> buildOrderBy(SortDirection direction) {
        return SortDirection.ASC == direction
                ? POPULAR_BOOKS.RANK.asc()
                : POPULAR_BOOKS.RANK.desc();
    }
}
