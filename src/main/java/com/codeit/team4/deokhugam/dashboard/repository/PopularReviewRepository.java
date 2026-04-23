package com.codeit.team4.deokhugam.dashboard.repository;

import com.codeit.team4.deokhugam.dashboard.entity.PeriodType;
import com.codeit.team4.deokhugam.dashboard.entity.PopularReview;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PopularReviewRepository extends JpaRepository<PopularReview, UUID> {

    @Modifying
    @Query("DELETE FROM PopularReview pr WHERE pr.period = :period AND pr.snapshotDate = :snapshotDate")
    void deleteByPeriodAndSnapshotDate(
            @Param("period") PeriodType period,
            @Param("snapshotDate") LocalDate snapshotDate
    );
}
