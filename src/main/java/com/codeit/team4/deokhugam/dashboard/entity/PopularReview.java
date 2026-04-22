package com.codeit.team4.deokhugam.dashboard.entity;

import com.codeit.team4.deokhugam.book.entity.Book;
import com.codeit.team4.deokhugam.global.entity.BaseEntity;
import com.codeit.team4.deokhugam.review.entity.Review;
import com.codeit.team4.deokhugam.user.entity.User;
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
        name = "popular_reviews",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_popular_reviews",
                columnNames = {"period", "review_id", "snapshot_date"}
        ),
        indexes = @Index(name = "idx_popular_reviews_period_rank", columnList = "period, rank")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PopularReview extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 255)
    private String bookTitle;

    @Column(length = 500)
    private String bookThumbnailUrl;

    @Column(nullable = false, length = 50)
    private String userNickname;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reviewContent;

    @Column(nullable = false)
    private int reviewRating;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PeriodType period;

    @Column(name = "rank", nullable = false)
    private int rank;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal score;

    @Column(nullable = false)
    private int likeCount;

    @Column(nullable = false)
    private int commentCount;

    @Column(nullable = false)
    private LocalDate snapshotDate;

    public PopularReview(
            Review review,
            Book book,
            User user,
            String bookTitle,
            String bookThumbnailUrl,
            String userNickname,
            String reviewContent,
            int reviewRating,
            PeriodType period,
            int rank,
            BigDecimal score,
            int likeCount,
            int commentCount,
            LocalDate snapshotDate
    ) {
        this.review = review;
        this.book = book;
        this.user = user;
        this.bookTitle = bookTitle;
        this.bookThumbnailUrl = bookThumbnailUrl;
        this.userNickname = userNickname;
        this.reviewContent = reviewContent;
        this.reviewRating = reviewRating;
        this.period = period;
        this.rank = rank;
        this.score = score;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.snapshotDate = snapshotDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PopularReview other)) return false;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
