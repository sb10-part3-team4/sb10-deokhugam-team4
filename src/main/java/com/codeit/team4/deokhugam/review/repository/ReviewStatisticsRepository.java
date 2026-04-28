package com.codeit.team4.deokhugam.review.repository;

import com.codeit.team4.deokhugam.review.entity.ReviewStatistics;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewStatisticsRepository extends JpaRepository<ReviewStatistics, UUID> {

}
