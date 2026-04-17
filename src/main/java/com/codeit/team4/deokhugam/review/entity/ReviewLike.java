package com.codeit.team4.deokhugam.review.entity;

import com.codeit.team4.deokhugam.global.entity.BaseEntity;
import com.codeit.team4.deokhugam.user.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
        name = "review_likes",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_review_likes",
                columnNames = {"review_id", "user_id"}
        ),
        indexes = @Index(name = "idx_review_likes_user_id", columnList = "user_id")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewLike extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public ReviewLike(Review review, User user) {
        this.review = review;
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReviewLike other)) return false;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
