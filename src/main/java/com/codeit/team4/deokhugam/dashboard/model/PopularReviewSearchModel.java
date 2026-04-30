package com.codeit.team4.deokhugam.dashboard.model;

import static com.codeit.team4.deokhugam.jooq.tables.Books.BOOKS;
import static com.codeit.team4.deokhugam.jooq.tables.Reviews.REVIEWS;
import static com.codeit.team4.deokhugam.jooq.tables.Users.USERS;

import java.util.List;
import java.util.UUID;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.impl.DSL;

public record PopularReviewSearchModel(
        UUID reviewId,
        UUID bookId,
        String bookTitle,
        String bookThumbnailUrl,
        UUID userId,
        String userNickname,
        String reviewContent,
        int reviewRating,
        int likeCount,
        int commentCount
) {

    public static final Field<Integer> PERIOD_LIKE_COUNT =
            DSL.coalesce(DSL.field("period_likes.like_count", Integer.class), 0);

    public static final Field<Integer> PERIOD_COMMENT_COUNT =
            DSL.coalesce(DSL.field("period_comments.comment_count", Integer.class), 0);

    public static List<Field<?>> toSelectedFields() {
        return List.of(
                REVIEWS.ID,
                REVIEWS.BOOK_ID,
                BOOKS.TITLE,
                BOOKS.THUMBNAIL_URL,
                REVIEWS.USER_ID,
                USERS.NICKNAME,
                REVIEWS.CONTENT,
                REVIEWS.RATING,
                PERIOD_LIKE_COUNT.as("period_like_count"),
                PERIOD_COMMENT_COUNT.as("period_comment_count")
        );
    }

    public static PopularReviewSearchModel fromRecord(Record rec) {
        return new PopularReviewSearchModel(
                rec.get(REVIEWS.ID),
                rec.get(REVIEWS.BOOK_ID),
                rec.get(BOOKS.TITLE),
                rec.get(BOOKS.THUMBNAIL_URL),
                rec.get(REVIEWS.USER_ID),
                rec.get(USERS.NICKNAME),
                rec.get(REVIEWS.CONTENT),
                rec.get(REVIEWS.RATING),
                rec.get("period_like_count", Integer.class),
                rec.get("period_comment_count", Integer.class)
        );
    }
}
