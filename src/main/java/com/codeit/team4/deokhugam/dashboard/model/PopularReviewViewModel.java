package com.codeit.team4.deokhugam.dashboard.model;

import static com.codeit.team4.deokhugam.jooq.tables.PopularReviews.POPULAR_REVIEWS;

import com.codeit.team4.deokhugam.dashboard.entity.PeriodType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.jooq.Field;
import org.jooq.Record;

public record PopularReviewViewModel(
        UUID id,
        UUID reviewId,
        UUID bookId,
        String bookTitle,
        String bookThumbnailUrl,
        UUID userId,
        String userNickname,
        String reviewContent,
        int reviewRating,
        PeriodType period,
        int rank,
        BigDecimal score,
        int likeCount,
        int commentCount,
        Instant createdAt
) implements RankedViewModel {

    public static List<Field<?>> toSelectedFields() {
        return List.of(
                POPULAR_REVIEWS.ID,
                POPULAR_REVIEWS.REVIEW_ID,
                POPULAR_REVIEWS.BOOK_ID,
                POPULAR_REVIEWS.BOOK_TITLE,
                POPULAR_REVIEWS.BOOK_THUMBNAIL_URL,
                POPULAR_REVIEWS.USER_ID,
                POPULAR_REVIEWS.USER_NICKNAME,
                POPULAR_REVIEWS.REVIEW_CONTENT,
                POPULAR_REVIEWS.REVIEW_RATING,
                POPULAR_REVIEWS.PERIOD,
                POPULAR_REVIEWS.RANK,
                POPULAR_REVIEWS.SCORE,
                POPULAR_REVIEWS.LIKE_COUNT,
                POPULAR_REVIEWS.COMMENT_COUNT,
                POPULAR_REVIEWS.CREATED_AT
        );
    }

    public static PopularReviewViewModel fromRecord(Record rec) {
        return new PopularReviewViewModel(
                rec.get(POPULAR_REVIEWS.ID),
                rec.get(POPULAR_REVIEWS.REVIEW_ID),
                rec.get(POPULAR_REVIEWS.BOOK_ID),
                rec.get(POPULAR_REVIEWS.BOOK_TITLE),
                rec.get(POPULAR_REVIEWS.BOOK_THUMBNAIL_URL),
                rec.get(POPULAR_REVIEWS.USER_ID),
                rec.get(POPULAR_REVIEWS.USER_NICKNAME),
                rec.get(POPULAR_REVIEWS.REVIEW_CONTENT),
                rec.get(POPULAR_REVIEWS.REVIEW_RATING),
                PeriodType.valueOf(rec.get(POPULAR_REVIEWS.PERIOD)),
                rec.get(POPULAR_REVIEWS.RANK),
                rec.get(POPULAR_REVIEWS.SCORE),
                rec.get(POPULAR_REVIEWS.LIKE_COUNT),
                rec.get(POPULAR_REVIEWS.COMMENT_COUNT),
                rec.get(POPULAR_REVIEWS.CREATED_AT).toInstant()
        );
    }
}
