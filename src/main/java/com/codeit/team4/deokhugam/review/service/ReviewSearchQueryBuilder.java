package com.codeit.team4.deokhugam.review.service;

import static com.codeit.team4.deokhugam.jooq.tables.Books.BOOKS;
import static com.codeit.team4.deokhugam.jooq.tables.Reviews.REVIEWS;
import static com.codeit.team4.deokhugam.jooq.tables.Users.USERS;

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
        return DSL.and(
                REVIEWS.DELETED_AT.isNull(),
                keywordCondition(param),
                userIdCondition(param),
                bookIdCondition(param),
                cursorCondition(param)
        );
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

    private Condition keywordCondition(ReviewSearchRequestParam param) {
        if (param.keyword() == null || param.keyword().isBlank()) {
            return DSL.noCondition();
        }
        return DSL.or(
                USERS.NICKNAME.containsIgnoreCase(param.keyword()),
                REVIEWS.CONTENT.containsIgnoreCase(param.keyword()),
                BOOKS.TITLE.containsIgnoreCase(param.keyword())
        );
    }

    private Condition userIdCondition(ReviewSearchRequestParam param) {
        if (param.userId() == null) {
            return DSL.noCondition();
        }
        return REVIEWS.USER_ID.eq(param.userId());
    }

    private Condition bookIdCondition(ReviewSearchRequestParam param) {
        if (param.bookId() == null) {
            return DSL.noCondition();
        }
        return REVIEWS.BOOK_ID.eq(param.bookId());
    }

    private Condition cursorCondition(ReviewSearchRequestParam param) {
        if (param.cursor() == null || param.after() == null) {
            return DSL.noCondition();
        }

        boolean isAsc = SortDirection.ASC == param.direction();
        OffsetDateTime afterTime = param.after().atOffset(ZoneOffset.UTC);

        if (param.orderBy().isRating()) {
            Integer cursorValue = param.cursorAsInteger();
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
