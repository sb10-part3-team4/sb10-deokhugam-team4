package com.codeit.team4.deokhugam.comment.repository;

import com.codeit.team4.deokhugam.comment.entity.Comment;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    // 논리 삭제: deleted_at이 NULL일 때만 현재 시간으로 업데이트
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Comment c SET c.deletedAt = :now WHERE c.id = :id AND c.deletedAt IS NULL")
    int softDeleteWithCondition(@Param("id") UUID id, @Param("now") Instant now);

    // 물리 삭제: deleted_at이 NULL일 때만 Row 삭제
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Comment c WHERE c.id = :id AND c.deletedAt IS NULL")
    int hardDeleteWithCondition(@Param("id") UUID id);

    // 물리 삭제: 상태와 상관없이 삭제할 때 사용(이미 논리 삭제된 상태일 때)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Comment c WHERE c.id = :id")
    int hardDeleteForce(@Param("id") UUID id);
}
