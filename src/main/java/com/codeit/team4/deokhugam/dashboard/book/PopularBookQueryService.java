package com.codeit.team4.deokhugam.dashboard.book;

import static com.codeit.team4.deokhugam.jooq.tables.Books.BOOKS;
import static com.codeit.team4.deokhugam.jooq.tables.Reviews.REVIEWS;

import com.codeit.team4.deokhugam.dashboard.entity.PeriodType;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PopularBookQueryService {

    private static final int POPULAR_BOOK_LIMIT = 4;

    private final DSLContext dsl;
    private final PopularBookQueryBuilder queryBuilder;

    public List<PopularBookSearchModel> findTopBooks(PeriodType period, LocalDate snapshotDate) {
        return dsl.select(PopularBookSearchModel.toSelectedFields())
                .from(REVIEWS)
                .join(BOOKS).on(REVIEWS.BOOK_ID.eq(BOOKS.ID))
                .where(queryBuilder.buildCondition(period, snapshotDate))
                .groupBy(PopularBookSearchModel.toGroupByFields())
                .orderBy(queryBuilder.buildOrderBy())
                .limit(POPULAR_BOOK_LIMIT)
                .fetch(PopularBookSearchModel::fromRecord);
    }
}
