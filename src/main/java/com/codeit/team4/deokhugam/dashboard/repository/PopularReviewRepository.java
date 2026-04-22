package com.codeit.team4.deokhugam.dashboard.repository;

import com.codeit.team4.deokhugam.dashboard.entity.PopularReview;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PopularReviewRepository extends JpaRepository<PopularReview, UUID> {

}
