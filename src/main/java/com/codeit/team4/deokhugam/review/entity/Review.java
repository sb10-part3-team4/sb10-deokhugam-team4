package com.codeit.team4.deokhugam.review.entity;

import com.codeit.team4.deokhugam.book.entity.Book;
import com.codeit.team4.deokhugam.global.entity.BaseUpdatableEntity;
import com.codeit.team4.deokhugam.global.error.BusinessException;
import com.codeit.team4.deokhugam.global.error.ErrorCode;
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
        name = "reviews",
        indexes = {
                @Index(name = "idx_reviews_created_at", columnList = "created_at"),
                @Index(name = "idx_reviews_rating", columnList = "rating"),
                @Index(name = "idx_reviews_user_id", columnList = "user_id"),
                @Index(name = "idx_reviews_deleted_at", columnList = "deleted_at")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseUpdatableEntity {

    private static final int MIN_RATING = 1;
    private static final int MAX_RATING = 5;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private int rating;

    @Column(nullable = false)
    private int likeCount;

    @Column(nullable = false)
    private int commentCount;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public Review(Book book, User user, String content, int rating) {
        validateRating(rating);
        this.book = book;
        this.user = user;
        this.content = content;
        this.rating = rating;
        this.likeCount = 0;
        this.commentCount = 0;
    }

    private void validateRating(int rating) {
        if (rating < MIN_RATING || rating > MAX_RATING) {
            throw new BusinessException(
                    ErrorCode.INVALID_RATING,
                    "rating=" + rating
            );
        }
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Review other)) {
            return false;
        }
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
