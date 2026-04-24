package com.codeit.team4.deokhugam.dashboard.repository;

import com.codeit.team4.deokhugam.dashboard.entity.PeriodType;
import com.codeit.team4.deokhugam.dashboard.entity.PowerUser;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PowerUserRepository extends JpaRepository<PowerUser, UUID> {

    @Modifying
    @Query("DELETE FROM PowerUser pu WHERE pu.period = :period AND pu.snapshotDate = :snapshotDate")
    void deleteByPeriodAndSnapshotDate(
            @Param("period") PeriodType period,
            @Param("snapshotDate") LocalDate snapshotDate
    );
}
