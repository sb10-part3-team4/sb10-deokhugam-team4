package com.codeit.team4.deokhugam.review.service;

import static com.codeit.team4.deokhugam.jooq.tables.Books.BOOKS;
import static com.codeit.team4.deokhugam.jooq.tables.Reviews.REVIEWS;
import static com.codeit.team4.deokhugam.jooq.tables.Users.USERS;

import com.codeit.team4.deokhugam.global.response.PageResponse;
import com.codeit.team4.deokhugam.jooq.tables.Books;
import com.codeit.team4.deokhugam.jooq.tables.Reviews;
import com.codeit.team4.deokhugam.jooq.tables.Users;
import com.codeit.team4.deokhugam.review.dto.ReviewResponse;
import com.codeit.team4.deokhugam.review.model.ReviewSearchModel;
import com.codeit.team4.deokhugam.review.dto.ReviewSearchRequestParam;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReviewQueryService {

    private final DSLContext dsl;
    private final ReviewSearchQueryBuilder queryBuilder;

    public PageResponse<ReviewResponse> searchReviews(ReviewSearchRequestParam param) {

        List<ReviewSearchModel> results = dsl.select(ReviewSearchModel.toSelectedFields(param.requestUserId()))
                .from(REVIEWS)
                .join(BOOKS).on(REVIEWS.BOOK_ID.eq(BOOKS.ID))
                .join(USERS).on(REVIEWS.USER_ID.eq(USERS.ID))
                .where(queryBuilder.buildCondition(param))
                .orderBy(queryBuilder.buildOrderBy(param))
                .limit(param.limit() + 1)
                .fetch(ReviewSearchModel::fromRecord);

        //limit 요청만큼 자르기
        boolean hasNext = results.size() > param.limit();
        List<ReviewSearchModel> content = hasNext
                ? results.subList(0, param.limit()) : results;

        //createdAt, rating 으로 정렬 조건
        String nextCursor = null;
        Instant nextAfter = null;
        if (hasNext && !content.isEmpty()) {
            ReviewSearchModel last = content.get(content.size() - 1);
            nextCursor = param.orderBy().isRating()
                    ? String.valueOf(last.rating())
                    : last.createdAt().toString();
            nextAfter = last.createdAt();
        }

        List<ReviewResponse> reviews = content.stream()
                .map(ReviewResponse::from)
                .toList();

        long totalElements = getTotalElements(param);

        return new PageResponse<>(reviews, nextCursor, nextAfter, param.limit(), totalElements, hasNext);
    }

    private long getTotalElements(ReviewSearchRequestParam param) {
        return Optional.ofNullable(
                dsl.selectCount()
                        .from(REVIEWS)
                        .join(BOOKS).on(REVIEWS.BOOK_ID.eq(BOOKS.ID))
                        .join(USERS).on(REVIEWS.USER_ID.eq(USERS.ID))
                        .where(conditionBuilder.toFilterCondition(param))
                        .fetchOne(0, Long.class))
                .orElse(0L);
    }
}
