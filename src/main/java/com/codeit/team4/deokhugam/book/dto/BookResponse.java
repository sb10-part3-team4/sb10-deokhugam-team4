package com.codeit.team4.deokhugam.book.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record BookResponse(
        UUID id,
        String title,
        String author,
        String description,
        String publisher,
        LocalDate publishedDate,
        String isbn,
        String thumbnailUrl,
        int reviewCount,
        BigDecimal rating,
        Instant createdAt,
        Instant updatedAt
) {

}
