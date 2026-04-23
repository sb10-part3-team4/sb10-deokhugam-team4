package com.codeit.team4.deokhugam.dashboard.entity;

import com.codeit.team4.deokhugam.global.entity.BaseEntity;
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
        name = "power_users",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_power_users",
                columnNames = {"period", "user_id", "snapshot_date"}
        ),
        indexes = @Index(name = "idx_power_users_period_rank", columnList = "period, rank")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PowerUser extends BaseEntity {

    public static final BigDecimal REVIEW_SCORE_SUM_WEIGHT = new BigDecimal("0.5");
    public static final BigDecimal LIKE_COUNT_WEIGHT = new BigDecimal("0.2");
    public static final BigDecimal COMMENT_COUNT_WEIGHT = new BigDecimal("0.3");

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PeriodType period;

    @Column(nullable = false)
    private int rank;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal score;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal reviewScoreSum;

    @Column(nullable = false)
    private int likeCount;

    @Column(nullable = false)
    private int commentCount;

    @Column(nullable = false)
    private LocalDate snapshotDate;

    public PowerUser(
            User user,
            String nickname,
            PeriodType period,
            int rank,
            BigDecimal reviewScoreSum,
            int likeCount,
            int commentCount,
            LocalDate snapshotDate
    ) {
        this.user = user;
        this.nickname = nickname;
        this.period = period;
        this.rank = rank;
        this.score = calculateScore(reviewScoreSum, likeCount, commentCount);
        this.reviewScoreSum = reviewScoreSum;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.snapshotDate = snapshotDate;
    }

    private BigDecimal calculateScore(
            BigDecimal reviewScoreSum,
            int likeCount,
            int commentCount
    ) {
        return reviewScoreSum.multiply(REVIEW_SCORE_SUM_WEIGHT)
                .add(new BigDecimal(likeCount).multiply(LIKE_COUNT_WEIGHT))
                .add(new BigDecimal(commentCount).multiply(COMMENT_COUNT_WEIGHT));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PowerUser other)) return false;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
