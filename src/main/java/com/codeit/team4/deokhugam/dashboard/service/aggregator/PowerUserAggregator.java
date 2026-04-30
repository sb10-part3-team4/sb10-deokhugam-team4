package com.codeit.team4.deokhugam.dashboard.service.aggregator;

import static com.codeit.team4.deokhugam.jooq.tables.Reviews.REVIEWS;
import static com.codeit.team4.deokhugam.jooq.tables.Users.USERS;

import com.codeit.team4.deokhugam.dashboard.builder.PowerUserAggregationQueryBuilder;
import com.codeit.team4.deokhugam.dashboard.entity.PeriodType;
import com.codeit.team4.deokhugam.dashboard.model.PowerUserSearchModel;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Table;
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
        Table<?> periodLikes = aggregationQueryBuilder.buildPeriodLikeCountTable(period, snapshotDate);
        Table<?> periodComments = aggregationQueryBuilder.buildPeriodCommentCountTable(period, snapshotDate);

        return dsl.select(PowerUserSearchModel.toSelectedFields())
                .from(REVIEWS)
                .join(USERS).on(REVIEWS.USER_ID.eq(USERS.ID))
                .leftJoin(periodLikes).on(periodLikes.field("review_id", UUID.class).eq(REVIEWS.ID))
                .leftJoin(periodComments).on(periodComments.field("review_id", UUID.class).eq(REVIEWS.ID))
                .where(aggregationQueryBuilder.buildCondition(period, snapshotDate))
                .groupBy(PowerUserSearchModel.toGroupByFields())
                .orderBy(aggregationQueryBuilder.buildOrderBy(
                        PowerUserSearchModel.PERIOD_LIKE_COUNT,
                        PowerUserSearchModel.PERIOD_COMMENT_COUNT
                ))
                .limit(POWER_USER_LIMIT)
                .fetch(PowerUserSearchModel::fromRecord);
    }
}
