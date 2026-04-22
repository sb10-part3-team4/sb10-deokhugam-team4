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
                BOOKS.THUMBNAIL_URL
        );
    }

    public static PopularBookSearchModel fromRecord(Record record) {
        return new PopularBookSearchModel(
                record.get(REVIEWS.BOOK_ID),
                record.get(BOOKS.TITLE),
                record.get(BOOKS.AUTHOR),
                record.get(BOOKS.THUMBNAIL_URL),
                record.get(REVIEW_COUNT),
                record.get(AVG_RATING)
        );
    }
}
