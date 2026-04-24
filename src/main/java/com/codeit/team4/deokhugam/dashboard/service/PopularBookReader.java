package com.codeit.team4.deokhugam.dashboard.service;

import static com.codeit.team4.deokhugam.jooq.tables.PopularBooks.POPULAR_BOOKS;

import com.codeit.team4.deokhugam.dashboard.builder.PopularBookReadQueryBuilder;
import com.codeit.team4.deokhugam.dashboard.dto.DashboardSearchRequestParam;
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
public class PopularBookReader {

    private final DSLContext dsl;
    private final PopularBookReadQueryBuilder readQueryBuilder;

    public LocalDate findLatestSnapshotDate(PeriodType period) {
        return dsl.select(DSL.max(POPULAR_BOOKS.SNAPSHOT_DATE))
                .from(POPULAR_BOOKS)
                .where(POPULAR_BOOKS.PERIOD.eq(period.name()))
                .fetchOneInto(LocalDate.class);
    }

    public List<PopularBookViewModel> findPopularBooks(
            DashboardSearchRequestParam param,
            LocalDate snapshotDate
    ) {
        return dsl.select(PopularBookViewModel.toSelectedFields())
                .from(POPULAR_BOOKS)
                .where(readQueryBuilder.buildCondition(param, snapshotDate))
                .orderBy(readQueryBuilder.buildOrderBy(param.direction()))
                .limit(param.limit() + 1) // +1로 다음 페이지 존재 여부(hasNext) 판단
                .fetch(PopularBookViewModel::fromRecord);
    }

}
