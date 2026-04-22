package com.codeit.team4.deokhugam.review.service;

import static com.codeit.team4.deokhugam.jooq.tables.Books.BOOKS;
import static com.codeit.team4.deokhugam.jooq.tables.Reviews.REVIEWS;
import static com.codeit.team4.deokhugam.jooq.tables.Users.USERS;

import com.codeit.team4.deokhugam.global.error.BusinessException;
import com.codeit.team4.deokhugam.global.error.ErrorCode;
import com.codeit.team4.deokhugam.global.response.SortDirection;
import com.codeit.team4.deokhugam.review.dto.ReviewSearchRequestParam;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.jooq.Condition;
import org.jooq.SortField;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

@Component
public class ReviewSearchQueryBuilder {

    public Condition buildCondition(ReviewSearchRequestParam param) {
        Condition condition = REVIEWS.DELETED_AT.isNull();

        if (param.keyword() != null && !param.keyword().isBlank()) {
            condition = condition.and(
                    USERS.NICKNAME.containsIgnoreCase(param.keyword())
                            .or(REVIEWS.CONTENT.containsIgnoreCase(param.keyword()))
                            .or(BOOKS.TITLE.containsIgnoreCase(param.keyword()))
            );
        }
        if (param.userId() != null) {
            condition = condition.and(REVIEWS.USER_ID.eq(param.userId()));
        }
        if (param.bookId() != null) {
            condition = condition.and(REVIEWS.BOOK_ID.eq(param.bookId()));
        }
        if (param.cursor() != null && param.after() != null) {
            condition = condition.and(buildCursorCondition(param));
        }

        return condition;
    }

    public List<SortField<?>> buildOrderBy(ReviewSearchRequestParam param) {
        boolean isAsc = SortDirection.ASC == param.direction();
        List<SortField<?>> orderBy = new ArrayList<>();

        if (param.orderBy().isRating()) {
            orderBy.add(isAsc ? REVIEWS.RATING.asc() : REVIEWS.RATING.desc());
        }
        orderBy.add(isAsc ? REVIEWS.CREATED_AT.asc() : REVIEWS.CREATED_AT.desc());
        orderBy.add(isAsc ? REVIEWS.ID.asc() : REVIEWS.ID.desc());

        return orderBy;
    }

    private Condition buildCursorCondition(ReviewSearchRequestParam param) {
        boolean isAsc = SortDirection.ASC == param.direction();
        OffsetDateTime afterTime = param.after().atOffset(ZoneOffset.UTC);

        if (param.orderBy().isRating()) {
            int cursorValue;
            try {
                cursorValue = Integer.parseInt(param.cursor());
            } catch (NumberFormatException e) {
                throw new BusinessException(ErrorCode.INVALID_INPUT, "rating 정렬에서 cursor는 정수여야 합니다");
            }
            return isAsc
                    ? DSL.row(REVIEWS.RATING, REVIEWS.CREATED_AT).gt(cursorValue, afterTime)
                    : DSL.row(REVIEWS.RATING, REVIEWS.CREATED_AT).lt(cursorValue, afterTime);
        } else {
            return isAsc
                    ? REVIEWS.CREATED_AT.gt(afterTime)
                    : REVIEWS.CREATED_AT.lt(afterTime);
        }
    }
}
