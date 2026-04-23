package com.codeit.team4.deokhugam.dashboard.service;

import static com.codeit.team4.deokhugam.jooq.tables.PopularBooks.POPULAR_BOOKS;

import com.codeit.team4.deokhugam.dashboard.builder.PopularBookViewQueryBuilder;
import com.codeit.team4.deokhugam.dashboard.dto.PopularBookSearchRequestParam;
import com.codeit.team4.deokhugam.dashboard.entity.PeriodType;
import com.codeit.team4.deokhugam.dashboard.model.PopularBookViewModel;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PopularBookQueryService {

    private final DSLContext dsl;
    private final PopularBookViewQueryBuilder viewQueryBuilder;

    public LocalDate findLatestSnapshotDate(PeriodType period) {
        return dsl.select(DSL.max(POPULAR_BOOKS.SNAPSHOT_DATE))
                .from(POPULAR_BOOKS)
                .where(POPULAR_BOOKS.PERIOD.eq(period.name()))
                .fetchOneInto(LocalDate.class);
    }

    public List<PopularBookViewModel> findPopularBooks(
            PopularBookSearchRequestParam param,
            LocalDate snapshotDate
    ) {
        return dsl.select(PopularBookViewModel.toSelectedFields())
                .from(POPULAR_BOOKS)
                .where(viewQueryBuilder.buildCondition(param, snapshotDate))
                .orderBy(viewQueryBuilder.buildOrderBy(param.direction()))
                .limit(param.limit() + 1)
                .fetch(PopularBookViewModel::fromRecord);
    }

}
