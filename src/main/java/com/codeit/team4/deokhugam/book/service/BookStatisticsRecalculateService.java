package com.codeit.team4.deokhugam.book.service;

import static com.codeit.team4.deokhugam.jooq.tables.BookStatistics.BOOK_STATISTICS;
import static com.codeit.team4.deokhugam.jooq.tables.Reviews.REVIEWS;
import static org.jooq.impl.DSL.coalesce;
import static org.jooq.impl.DSL.count;
import static org.jooq.impl.DSL.sum;
import static org.jooq.impl.DSL.val;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookStatisticsRecalculateService {

    private final DSLContext dsl;

    @Transactional
    public int recalculateBookStatisticsFromReviews() {
        log.info("BookStatistics 전체 재계산 시작");

        int rows = dsl.insertInto(
                        BOOK_STATISTICS,
                        BOOK_STATISTICS.BOOK_ID,
                        BOOK_STATISTICS.RATING_SUM,
                        BOOK_STATISTICS.REVIEW_COUNT
                )
                .select(
                        dsl.select(
                                        REVIEWS.BOOK_ID,
                                        coalesce(sum(REVIEWS.RATING), val(0)).cast(Integer.class),
                                        count()
                                )
                                .from(REVIEWS)
                                .where(REVIEWS.DELETED_AT.isNull())
                                .groupBy(REVIEWS.BOOK_ID)
                )
                .onConflict(BOOK_STATISTICS.BOOK_ID)
                .doUpdate()
                .set(BOOK_STATISTICS.RATING_SUM, BOOK_STATISTICS.as("excluded").field(BOOK_STATISTICS.RATING_SUM))
                .set(BOOK_STATISTICS.REVIEW_COUNT, BOOK_STATISTICS.as("excluded").field(BOOK_STATISTICS.REVIEW_COUNT))
                .execute();

        log.info("BookStatistics 전체 재계산 완료: {}건", rows);
        return rows;
    }
}
