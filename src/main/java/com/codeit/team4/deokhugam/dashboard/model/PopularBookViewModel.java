package com.codeit.team4.deokhugam.dashboard.model;

import static com.codeit.team4.deokhugam.jooq.tables.PopularBooks.POPULAR_BOOKS;

import com.codeit.team4.deokhugam.dashboard.entity.PeriodType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.jooq.Field;
import org.jooq.Record;

public record PopularBookViewModel(
        UUID id,
        UUID bookId,
        String title,
        String author,
        String thumbnailUrl,
        PeriodType period,
        int rank,
        BigDecimal score,
        int reviewCount,
        BigDecimal rating,
        Instant createdAt
) implements RankedViewModel {

    public static List<Field<?>> toSelectedFields() {
        return List.of(
                POPULAR_BOOKS.ID,
                POPULAR_BOOKS.BOOK_ID,
                POPULAR_BOOKS.TITLE,
                POPULAR_BOOKS.AUTHOR,
                POPULAR_BOOKS.THUMBNAIL_URL,
                POPULAR_BOOKS.PERIOD,
                POPULAR_BOOKS.RANK,
                POPULAR_BOOKS.SCORE,
                POPULAR_BOOKS.REVIEW_COUNT,
                POPULAR_BOOKS.RATING,
                POPULAR_BOOKS.CREATED_AT
        );
    }

    public static PopularBookViewModel fromRecord(Record rec) {
        return new PopularBookViewModel(
                rec.get(POPULAR_BOOKS.ID),
                rec.get(POPULAR_BOOKS.BOOK_ID),
                rec.get(POPULAR_BOOKS.TITLE),
                rec.get(POPULAR_BOOKS.AUTHOR),
                rec.get(POPULAR_BOOKS.THUMBNAIL_URL),
                PeriodType.valueOf(rec.get(POPULAR_BOOKS.PERIOD)),
                rec.get(POPULAR_BOOKS.RANK),
                rec.get(POPULAR_BOOKS.SCORE),
                rec.get(POPULAR_BOOKS.REVIEW_COUNT),
                rec.get(POPULAR_BOOKS.RATING),
                rec.get(POPULAR_BOOKS.CREATED_AT).toInstant()
        );
    }
}
