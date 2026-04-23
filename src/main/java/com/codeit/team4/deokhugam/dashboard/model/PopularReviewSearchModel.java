package com.codeit.team4.deokhugam.dashboard.model;

import static com.codeit.team4.deokhugam.jooq.tables.Books.BOOKS;
import static com.codeit.team4.deokhugam.jooq.tables.Reviews.REVIEWS;
import static com.codeit.team4.deokhugam.jooq.tables.Users.USERS;

import java.util.List;
import java.util.UUID;
import org.jooq.Field;
import org.jooq.Record;

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
                REVIEWS.LIKE_COUNT,
                REVIEWS.COMMENT_COUNT
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
                rec.get(REVIEWS.LIKE_COUNT),
                rec.get(REVIEWS.COMMENT_COUNT)
        );
    }
}
