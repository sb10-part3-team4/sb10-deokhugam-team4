package com.codeit.team4.deokhugam.dashboard.model;

import static com.codeit.team4.deokhugam.jooq.tables.Books.BOOKS;
import static com.codeit.team4.deokhugam.jooq.tables.Reviews.REVIEWS;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.impl.DSL;

public record PopularBookSearchModel(
        UUID bookId,
        String title,
        String author,
        String thumbnailUrl,
        int reviewCount,
        BigDecimal avgRating
) {

    private static final Field<Integer> REVIEW_COUNT = DSL.count().as("review_count");
    private static final Field<BigDecimal> AVG_RATING = DSL.avg(REVIEWS.RATING).as("avg_rating");

    public static List<Field<?>> toSelectedFields() {
        return List.of(
                REVIEWS.BOOK_ID,
                BOOKS.TITLE,
                BOOKS.AUTHOR,
                BOOKS.THUMBNAIL_URL,
                REVIEW_COUNT,
                AVG_RATING
        );
    }

    public static List<Field<?>> toGroupByFields() {
        return List.of(
                REVIEWS.BOOK_ID,
                BOOKS.TITLE,
                BOOKS.AUTHOR,
                BOOKS.THUMBNAIL_URL,
                BOOKS.CREATED_AT
        );
    }

    public static PopularBookSearchModel fromRecord(Record rec) {
        return new PopularBookSearchModel(
                rec.get(REVIEWS.BOOK_ID),
                rec.get(BOOKS.TITLE),
                rec.get(BOOKS.AUTHOR),
                rec.get(BOOKS.THUMBNAIL_URL),
                rec.get(REVIEW_COUNT),
                rec.get(AVG_RATING)
        );
    }
}
