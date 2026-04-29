package com.codeit.team4.deokhugam.dashboard.service;

import static com.codeit.team4.deokhugam.jooq.tables.ReviewStatistics.REVIEW_STATISTICS;
import static com.codeit.team4.deokhugam.jooq.tables.Reviews.REVIEWS;
import static com.codeit.team4.deokhugam.jooq.tables.Users.USERS;

import com.codeit.team4.deokhugam.dashboard.builder.PowerUserAggregationQueryBuilder;
import com.codeit.team4.deokhugam.dashboard.entity.PeriodType;
import com.codeit.team4.deokhugam.dashboard.model.PowerUserSearchModel;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PowerUserAggregator {

    private static final int POWER_USER_LIMIT = 10;

    private final DSLContext dsl;
    private final PowerUserAggregationQueryBuilder aggregationQueryBuilder;

    public List<PowerUserSearchModel> findTopPowerUsers(PeriodType period, LocalDate snapshotDate) {
        return dsl.select(PowerUserSearchModel.toSelectedFields())
                .from(REVIEWS)
                .join(USERS).on(REVIEWS.USER_ID.eq(USERS.ID))
                .leftJoin(REVIEW_STATISTICS).on(REVIEW_STATISTICS.REVIEW_ID.eq(REVIEWS.ID))
                .where(aggregationQueryBuilder.buildCondition(period, snapshotDate))
                .groupBy(PowerUserSearchModel.toGroupByFields())
                .orderBy(aggregationQueryBuilder.buildOrderBy())
                .limit(POWER_USER_LIMIT)
                .fetch(PowerUserSearchModel::fromRecord);
    }
}
