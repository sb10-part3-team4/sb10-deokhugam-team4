package com.codeit.team4.deokhugam.dashboard.service.reader;

import static com.codeit.team4.deokhugam.jooq.tables.PopularReviews.POPULAR_REVIEWS;

import com.codeit.team4.deokhugam.dashboard.builder.PopularReviewReadQueryBuilder;
import com.codeit.team4.deokhugam.dashboard.dto.DashboardSearchRequestParam;
import com.codeit.team4.deokhugam.dashboard.entity.PeriodType;
import com.codeit.team4.deokhugam.dashboard.model.PopularReviewViewModel;
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
public class PopularReviewReader {

    private final DSLContext dsl;
    private final PopularReviewReadQueryBuilder readQueryBuilder;

    public LocalDate findLatestSnapshotDate(PeriodType period) {
        return dsl.select(DSL.max(POPULAR_REVIEWS.SNAPSHOT_DATE))
                .from(POPULAR_REVIEWS)
                .where(POPULAR_REVIEWS.PERIOD.eq(period.name()))
                .fetchOneInto(LocalDate.class);
    }

    public List<PopularReviewViewModel> findPopularReviews(
            DashboardSearchRequestParam param,
            LocalDate snapshotDate
    ) {
        return dsl.select(PopularReviewViewModel.toSelectedFields())
                .from(POPULAR_REVIEWS)
                .where(readQueryBuilder.buildCondition(param, snapshotDate))
                .orderBy(readQueryBuilder.buildOrderBy(param.direction()))
                .limit(param.limit() + 1) // +1로 다음 페이지 존재 여부(hasNext) 판단
                .fetch(PopularReviewViewModel::fromRecord);
    }
}
