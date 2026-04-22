package com.codeit.team4.deokhugam.dashboard.entity;

import com.codeit.team4.deokhugam.book.entity.Book;
import com.codeit.team4.deokhugam.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
        name = "popular_books",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_popular_books",
                columnNames = {"period", "book_id", "snapshot_date"}
        ),
        indexes = @Index(name = "idx_popular_books_period_rank", columnList = "period, rank")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PopularBook extends BaseEntity {

    public static final BigDecimal REVIEW_COUNT_WEIGHT = new BigDecimal("0.4");
    public static final BigDecimal AVG_RATING_WEIGHT = new BigDecimal("0.6");

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 100)
    private String author;

    @Column(length = 500)
    private String thumbnailUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PeriodType period;

    @Column(name = "rank", nullable = false)
    private int ranking;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal score;

    @Column(nullable = false)
    private int reviewCount;

    @Column(nullable = false, precision = 3, scale = 2)
    private BigDecimal rating;

    @Column(nullable = false)
    private LocalDate snapshotDate;

    public PopularBook(
            Book book,
            String title,
            String author,
            String thumbnailUrl,
            PeriodType period,
            int ranking,
            int reviewCount,
            BigDecimal rating,
            LocalDate snapshotDate
    ) {
        this.book = book;
        this.title = title;
        this.author = author;
        this.thumbnailUrl = thumbnailUrl;
        this.period = period;
        this.ranking = ranking;
        this.score = calculateScore(reviewCount, rating);
        this.reviewCount = reviewCount;
        this.rating = rating;
        this.snapshotDate = snapshotDate;
    }

    private BigDecimal calculateScore(int reviewCount, BigDecimal avgRating) {
        return new BigDecimal(reviewCount).multiply(REVIEW_COUNT_WEIGHT)
                .add(avgRating.multiply(AVG_RATING_WEIGHT));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PopularBook other)) return false;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
