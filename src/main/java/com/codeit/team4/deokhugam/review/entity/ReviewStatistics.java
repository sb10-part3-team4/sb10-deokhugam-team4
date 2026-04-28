package com.codeit.team4.deokhugam.review.entity;

import com.codeit.team4.deokhugam.book.entity.BookStatistics;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "review_statistics")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewStatistics {

    @Id
    @Column(name = "review_id")
    private UUID reviewId;

    @Column(nullable = false)
    private int commentCount;

    public ReviewStatistics(UUID reviewId) {
        this.reviewId = reviewId;
        this.commentCount = 0;
    }

    public void onCommentCreated() {
        this.commentCount++;
    }

    public void onCommentDeleted() {
        if (this.commentCount > 0) {
            this.commentCount--;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ReviewStatistics other)) {
            return false;
        }
        return Objects.equals(reviewId, other.reviewId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(reviewId);
    }
}
