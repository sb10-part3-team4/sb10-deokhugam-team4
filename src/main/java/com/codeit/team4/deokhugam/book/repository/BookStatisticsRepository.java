package com.codeit.team4.deokhugam.book.repository;

import com.codeit.team4.deokhugam.book.entity.BookStatistics;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookStatisticsRepository extends JpaRepository<BookStatistics, UUID> {

}
