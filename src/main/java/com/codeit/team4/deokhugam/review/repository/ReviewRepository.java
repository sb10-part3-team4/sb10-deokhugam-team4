package com.codeit.team4.deokhugam.review.repository;

import com.codeit.team4.deokhugam.review.entity.Review;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    boolean existsByBookIdAndUserIdAndDeletedAtIsNull(UUID bookId, UUID userId);

    @Query("SELECT r FROM Review r JOIN FETCH r.book JOIN FETCH r.user WHERE r.id = :id AND r.deletedAt IS NULL")
    Optional<Review> findByIdAndDeletedAtIsNull(@Param("id") UUID id);

    // 동시성 제어 + 서버 부하를 고려해 원자적 UPDATE 처리
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Review r SET r.commentCount = r.commentCount + 1 WHERE r.id = :id")
    void increaseCommentCount(@Param("reviewId") UUID reviewId);
}
