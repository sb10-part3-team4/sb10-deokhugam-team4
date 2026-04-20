package com.codeit.team4.deokhugam.comment.entity;

import com.codeit.team4.deokhugam.global.entity.BaseUpdatableEntity;
import com.codeit.team4.deokhugam.review.entity.Review;
import com.codeit.team4.deokhugam.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
        name = "comments",
        indexes = {
                @Index(name = "idx_comments_review_id", columnList = "review_id"),
                @Index(name = "idx_comments_user_id", columnList = "user_id"),
                @Index(name = "idx_comments_deleted_at", columnList = "deleted_at"),
                @Index(name = "idx_comments_created_at", columnList = "created_at")
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseUpdatableEntity {

    @ManyToOne(fetch = FetchType.LAZY) // N+1 방지를 위해 항상 LAZY
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public Comment(User user, Review review, String content) {
        this.user = user;
        this.review = review;
        this.content = content;
    }

    public void updateContent(String newContent) {
        this.content = newContent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Comment comment)) {
            return false;
        }
        return this.getId() != null && Objects.equals(this.getId(), comment.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
