package com.codeit.team4.deokhugam.dashboard.builder;

import static com.codeit.team4.deokhugam.jooq.tables.PowerUsers.POWER_USERS;

import com.codeit.team4.deokhugam.dashboard.dto.DashboardSearchRequestParam;
import com.codeit.team4.deokhugam.global.response.SortDirection;
import java.time.LocalDate;
import org.jooq.Condition;
import org.jooq.SortField;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

@Component
public class PowerUserReadQueryBuilder {

    public Condition buildCondition(DashboardSearchRequestParam param, LocalDate latestSnapshotDate) {
        return DSL.and(
                POWER_USERS.PERIOD.eq(param.period().name()),
                POWER_USERS.SNAPSHOT_DATE.eq(latestSnapshotDate),
                rankCondition(param)
        );
    }

    public SortField<?> buildOrderBy(SortDirection direction) {
        return SortDirection.ASC == direction
                ? POWER_USERS.RANK.asc()
                : POWER_USERS.RANK.desc();
    }

    private Condition rankCondition(DashboardSearchRequestParam param) {
        Integer cursor = param.cursorAsInteger();

        if (cursor == null) {
            return DSL.noCondition();
        }

        return switch (param.direction()) {
            case ASC -> POWER_USERS.RANK.gt(cursor);
            case DESC -> POWER_USERS.RANK.lt(cursor);
        };
    }
}
