package com.codeit.team4.deokhugam.review.service;

import static com.codeit.team4.deokhugam.jooq.tables.Books.BOOKS;
import static com.codeit.team4.deokhugam.jooq.tables.Reviews.REVIEWS;
import static com.codeit.team4.deokhugam.jooq.tables.Users.USERS;

import com.codeit.team4.deokhugam.global.response.PageResponse;
import com.codeit.team4.deokhugam.review.dto.ReviewResponse;
import com.codeit.team4.deokhugam.review.dto.ReviewSearchRequestParam;
import com.codeit.team4.deokhugam.review.mapper.ReviewMapper;
import com.codeit.team4.deokhugam.review.model.ReviewSearchModel;
import java.time.Instant;
import java.util.List;
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
    private final ReviewMapper reviewMapper;

    public PageResponse<ReviewResponse> searchReviews(ReviewSearchRequestParam param) {
        List<ReviewSearchModel> results = dsl.select(ReviewSearchModel.toSelectedFields(param.requestUserId()))
                .from(REVIEWS)
                .join(BOOKS).on(REVIEWS.BOOK_ID.eq(BOOKS.ID))
                .join(USERS).on(REVIEWS.USER_ID.eq(USERS.ID))
                .where(queryBuilder.buildCondition(param))
                .orderBy(queryBuilder.buildOrderBy(param))
                .limit(param.limit() + 1) // +1로 다음 페이지 존재 여부(hasNext) 판단
                .fetch(ReviewSearchModel::fromRecord);

        List<ReviewSearchModel> content = trimToLimit(results, param.limit());
        boolean hasNext = results.size() > param.limit();

        List<ReviewResponse> reviews = content.stream()
                .map(reviewMapper::toResponse)
                .toList();

        return new PageResponse<>(
                reviews,
                extractNextCursor(content, hasNext, param),
                extractNextAfter(content, hasNext),
                param.limit(),
                null,
                hasNext
        );
    }

    private List<ReviewSearchModel> trimToLimit(List<ReviewSearchModel> results, int limit) {
        return results.size() > limit
                ? results.subList(0, limit) : results;
    }

    private String extractNextCursor(
            List<ReviewSearchModel> content,
            boolean hasNext,
            ReviewSearchRequestParam param
    ) {
        if (!hasNext || content.isEmpty()) {
            return null;
        }
        ReviewSearchModel last = content.get(content.size() - 1);
        return param.orderBy().isRating()
                ? String.valueOf(last.rating())
                : last.createdAt().toString();
    }

    private Instant extractNextAfter(List<ReviewSearchModel> content, boolean hasNext) {
        if (!hasNext || content.isEmpty()) {
            return null;
        }
        return content.get(content.size() - 1).createdAt();
    }
}
