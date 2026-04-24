package com.codeit.team4.deokhugam.dashboard.service;

import static com.codeit.team4.deokhugam.jooq.tables.PowerUsers.POWER_USERS;

import com.codeit.team4.deokhugam.dashboard.builder.PowerUserReadQueryBuilder;
import com.codeit.team4.deokhugam.dashboard.dto.DashboardSearchRequestParam;
import com.codeit.team4.deokhugam.dashboard.entity.PeriodType;
import com.codeit.team4.deokhugam.dashboard.model.PowerUserViewModel;
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
public class PowerUserReader {

    private final DSLContext dsl;
    private final PowerUserReadQueryBuilder readQueryBuilder;

    public LocalDate findLatestSnapshotDate(PeriodType period) {
        return dsl.select(DSL.max(POWER_USERS.SNAPSHOT_DATE))
                .from(POWER_USERS)
                .where(POWER_USERS.PERIOD.eq(period.name()))
                .fetchOneInto(LocalDate.class);
    }

    public List<PowerUserViewModel> findPowerUsers(
            DashboardSearchRequestParam param,
            LocalDate snapshotDate
    ) {
        return dsl.select(PowerUserViewModel.toSelectedFields())
                .from(POWER_USERS)
                .where(readQueryBuilder.buildCondition(param, snapshotDate))
                .orderBy(readQueryBuilder.buildOrderBy(param.direction()))
                .limit(param.limit() + 1) // +1로 다음 페이지 존재 여부(hasNext) 판단
                .fetch(PowerUserViewModel::fromRecord);
    }
}
