package com.codeit.team4.deokhugam.review.model;

import static com.codeit.team4.deokhugam.jooq.tables.Books.BOOKS;
import static com.codeit.team4.deokhugam.jooq.tables.ReviewLikes.REVIEW_LIKES;
import static com.codeit.team4.deokhugam.jooq.tables.Reviews.REVIEWS;
import static com.codeit.team4.deokhugam.jooq.tables.Users.USERS;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.impl.DSL;

public record ReviewSearchModel(
        UUID id,
        UUID bookId,
        String bookTitle,
        String bookThumbnailUrl,
        UUID userId,
        String userNickname,
        String content,
        int rating,
        int likeCount,
        int commentCount,
        boolean likedByMe,
        Instant createdAt,
        Instant updatedAt
) {

    public static List<Field<?>> toSelectedFields(UUID requestUserId) {
        Field<Boolean> likedByMe = DSL.exists(
                DSL.selectOne()
                        .from(REVIEW_LIKES)
                        .where(REVIEW_LIKES.REVIEW_ID.eq(REVIEWS.ID)
                                .and(REVIEW_LIKES.USER_ID.eq(requestUserId)))
        ).as("liked_by_me");

        return new ArrayList<>(List.of(
                REVIEWS.ID,
                REVIEWS.BOOK_ID,
                BOOKS.TITLE,
                BOOKS.THUMBNAIL_URL,
                REVIEWS.USER_ID,
                USERS.NICKNAME,
                REVIEWS.CONTENT,
                REVIEWS.RATING,
                REVIEWS.LIKE_COUNT,
                REVIEWS.COMMENT_COUNT,
                likedByMe,
                REVIEWS.CREATED_AT,
                REVIEWS.UPDATED_AT
        ));
    }

    public static ReviewSearchModel fromRecord(Record rec) {
        return new ReviewSearchModel(
                rec.get(REVIEWS.ID),
                rec.get(REVIEWS.BOOK_ID),
                rec.get(BOOKS.TITLE),
                rec.get(BOOKS.THUMBNAIL_URL),
                rec.get(REVIEWS.USER_ID),
                rec.get(USERS.NICKNAME),
                rec.get(REVIEWS.CONTENT),
                rec.get(REVIEWS.RATING),
                rec.get(REVIEWS.LIKE_COUNT),
                rec.get(REVIEWS.COMMENT_COUNT),
                rec.get(DSL.field("liked_by_me", Boolean.class)),
                rec.get(REVIEWS.CREATED_AT).toInstant(),
                rec.get(REVIEWS.UPDATED_AT).toInstant()
        );
    }
}
