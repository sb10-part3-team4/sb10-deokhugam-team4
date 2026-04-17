package com.codeit.team4.deokhugam.review.repository;

import com.codeit.team4.deokhugam.review.entity.ReviewLike;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, UUID> {

    boolean existsByReviewIdAndUserId(UUID reviewId, UUID userId);
}
