package com.codeit.team4.deokhugam.dashboard.book;

import com.codeit.team4.deokhugam.dashboard.entity.PeriodType;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PopularBookRepository extends JpaRepository<PopularBook, UUID> {

    void deleteByPeriodAndSnapshotDate(PeriodType period, LocalDate snapshotDate);
}
