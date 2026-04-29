package com.codeit.team4.deokhugam.dashboard.service;

import static com.codeit.team4.deokhugam.jooq.tables.Books.BOOKS;
import static com.codeit.team4.deokhugam.jooq.tables.ReviewStatistics.REVIEW_STATISTICS;
import static com.codeit.team4.deokhugam.jooq.tables.Reviews.REVIEWS;
import static com.codeit.team4.deokhugam.jooq.tables.Users.USERS;

import com.codeit.team4.deokhugam.dashboard.builder.PopularReviewAggregationQueryBuilder;
import com.codeit.team4.deokhugam.dashboard.entity.PeriodType;
import com.codeit.team4.deokhugam.dashboard.model.PopularReviewSearchModel;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PopularReviewAggregator {

    private static final int POPULAR_REVIEW_LIMIT = 20;

    private final DSLContext dsl;
    private final PopularReviewAggregationQueryBuilder aggregationQueryBuilder;

    public List<PopularReviewSearchModel> findTopReviews(PeriodType period, LocalDate snapshotDate) {
        return dsl.select(PopularReviewSearchModel.toSelectedFields())
                .from(REVIEWS)
                .join(BOOKS).on(REVIEWS.BOOK_ID.eq(BOOKS.ID))
                .join(USERS).on(REVIEWS.USER_ID.eq(USERS.ID))
                .leftJoin(REVIEW_STATISTICS).on(REVIEW_STATISTICS.REVIEW_ID.eq(REVIEWS.ID))
                .where(aggregationQueryBuilder.buildCondition(period, snapshotDate))
                .orderBy(aggregationQueryBuilder.buildOrderBy())
                .limit(POPULAR_REVIEW_LIMIT)
                .fetch(PopularReviewSearchModel::fromRecord);
    }
}
