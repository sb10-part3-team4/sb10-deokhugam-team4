package com.codeit.team4.deokhugam.review.repository;

import static com.codeit.team4.deokhugam.jooq.tables.Books.BOOKS;
import static com.codeit.team4.deokhugam.jooq.tables.ReviewLikes.REVIEW_LIKES;
import static com.codeit.team4.deokhugam.jooq.tables.Reviews.REVIEWS;
import static com.codeit.team4.deokhugam.jooq.tables.Users.USERS;

import com.codeit.team4.deokhugam.global.response.PageResponse;
import com.codeit.team4.deokhugam.review.dto.ReviewOrderBy;
import com.codeit.team4.deokhugam.review.dto.ReviewResponse;
import com.codeit.team4.deokhugam.review.dto.ReviewSearchRequestParam;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReviewSearchRepository {

    private final DSLContext dsl;

    public PageResponse<ReviewResponse> searchReviews(ReviewSearchRequestParam param) {
        Field<Boolean> likedByMe = likedByMeField(param.requestUserId());

        List<ReviewResponse> results = dsl.select(
                        REVIEWS.ID,
                        REVIEWS.BOOK_ID,
                        BOOKS.TITLE,
                        BOOKS.THUMBNAIL_URL,
                        REVIEWS.USER_ID,
                        USERS.NICKNAME,
                        REVIEWS.CONTENT,
                        REVIEWS.RATING,
                        REVIEWS.LIKE_COUNT,
                        REVIEWS.COMMENT_COUNT,
                        likedByMe,
                        REVIEWS.CREATED_AT,
                        REVIEWS.UPDATED_AT
                )
                .from(REVIEWS)
                .join(BOOKS).on(REVIEWS.BOOK_ID.eq(BOOKS.ID))
                .join(USERS).on(REVIEWS.USER_ID.eq(USERS.ID))
                .where(param.toCondition())
                .orderBy(param.toOrderBy())
                .limit(param.limit() + 1)
                .fetch(record -> new ReviewResponse(
                        record.get(REVIEWS.ID),
                        record.get(REVIEWS.BOOK_ID),
                        record.get(BOOKS.TITLE),
                        record.get(BOOKS.THUMBNAIL_URL),
                        record.get(REVIEWS.USER_ID),
                        record.get(USERS.NICKNAME),
                        record.get(REVIEWS.CONTENT),
                        record.get(REVIEWS.RATING),
                        record.get(REVIEWS.LIKE_COUNT),
                        record.get(REVIEWS.COMMENT_COUNT),
                        record.get(likedByMe),
                        record.get(REVIEWS.CREATED_AT).toInstant(),
                        record.get(REVIEWS.UPDATED_AT).toInstant()
                ));

        //limit 요청만큼 자르기
        boolean hasNext = results.size() > param.limit();
        List<ReviewResponse> content = hasNext
                ? results.subList(0, param.limit()) : results;

        //createdAt, rating 으로 정렬 조건
        String nextCursor = null;
        Instant nextAfter = null;
        if (hasNext && !content.isEmpty()) {
            ReviewResponse last = content.get(content.size() - 1);
            nextCursor = ReviewOrderBy.rating == param.orderBy()
                    ? String.valueOf(last.rating())
                    : last.createdAt().toString();
            nextAfter = last.createdAt();
        }

        return new PageResponse<>(content, nextCursor, nextAfter, param.limit(), 0L, hasNext);
    }

    private Field<Boolean> likedByMeField(UUID requestUserId) {
        return DSL.exists(
                DSL.selectOne()
                        .from(REVIEW_LIKES)
                        .where(REVIEW_LIKES.REVIEW_ID.eq(REVIEWS.ID)
                                .and(REVIEW_LIKES.USER_ID.eq(requestUserId)))
        ).as("liked_by_me");
    }
}
