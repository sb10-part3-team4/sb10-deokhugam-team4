package com.codeit.team4.deokhugam.dashboard.repository;

import com.codeit.team4.deokhugam.dashboard.entity.PopularBook;
import com.codeit.team4.deokhugam.dashboard.entity.PeriodType;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PopularBookRepository extends JpaRepository<PopularBook, UUID> {

    @Modifying
    @Query("DELETE FROM PopularBook pb WHERE pb.period = :period AND pb.snapshotDate = :snapshotDate")
    void deleteByPeriodAndSnapshotDate(
            @Param("period") PeriodType period,
            @Param("snapshotDate") LocalDate snapshotDate
    );
}
