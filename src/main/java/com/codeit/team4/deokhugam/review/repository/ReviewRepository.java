package com.codeit.team4.deokhugam.review.repository;

import com.codeit.team4.deokhugam.review.entity.Review;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    boolean existsByBookIdAndUserIdAndDeletedAtIsNull(UUID bookId, UUID userId);

    Optional<Review> findByIdAndDeletedAtIsNull(UUID id);
}
