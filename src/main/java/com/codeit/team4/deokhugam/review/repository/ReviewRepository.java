package com.codeit.team4.deokhugam.review.repository;

import com.codeit.team4.deokhugam.review.entity.Review;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    @Query(value = "SELECT EXISTS (SELECT 1 FROM reviews r WHERE r.book_id = :bookId AND r.user_id = :userId AND r.deleted_at IS NULL)", nativeQuery = true)
    boolean existsByBookIdAndUserIdAndDeletedAtIsNull(@Param("bookId") UUID bookId, @Param("userId") UUID userId);

    @Query("SELECT r FROM Review r JOIN FETCH r.book JOIN FETCH r.user WHERE r.id = :id AND r.deletedAt IS NULL")
    Optional<Review> findByIdAndDeletedAtIsNull(@Param("id") UUID id);

    // 동시성 제어 + 서버 부하를 고려해 원자적 UPDATE 처리
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Review r SET r.commentCount = r.commentCount + 1 WHERE r.id = :reviewId")
    void increaseCommentCount(@Param("reviewId") UUID reviewId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Review r SET r.commentCount = r.commentCount - 1 WHERE r.id = :reviewId AND r.commentCount > 0")
    void decreaseCommentCount(@Param("reviewId") UUID reviewId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Review r SET r.likeCount = r.likeCount + 1 WHERE r.id = :reviewId")
    void increaseLikeCount(@Param("reviewId") UUID reviewId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Review r SET r.likeCount = r.likeCount - 1 WHERE r.id = :reviewId AND r.likeCount > 0")
    void decreaseLikeCount(@Param("reviewId") UUID reviewId);
}
