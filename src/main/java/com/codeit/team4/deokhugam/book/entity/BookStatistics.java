package com.codeit.team4.deokhugam.book.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "book_statistics")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookStatistics {

    @Id
    @Column(name = "book_id")
    private UUID bookId;

    @Column(nullable = false)
    private int ratingSum;

    @Column(nullable = false)
    private int reviewCount;

    public BookStatistics(UUID bookId) {
        this.bookId = bookId;
        this.ratingSum = 0;
        this.reviewCount = 0;
    }

    public void onReviewCreated(int rating) {
        this.reviewCount++;
        this.ratingSum += rating;
    }

    public void onReviewDeleted(int rating) {
        if (this.reviewCount <= 0) {
            return;
        }
        this.reviewCount--;
        this.ratingSum = Math.max(0, this.ratingSum - rating);
    }

    public void onReviewUpdated(
            int oldRating,
            int newRating
    ) {
        this.ratingSum += (newRating - oldRating);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BookStatistics other)) {
            return false;
        }
        return Objects.equals(bookId, other.bookId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(bookId);
    }

    public BigDecimal getAverageRating() {
        if (reviewCount == 0) {
            return BigDecimal.ZERO;
        }
        //소수점 2자리까지 반올림
        return BigDecimal.valueOf(ratingSum)
                .divide(BigDecimal.valueOf(reviewCount), 2, RoundingMode.HALF_UP);
    }
}
