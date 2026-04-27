package com.codeit.team4.deokhugam.book.dto;

import java.time.LocalDate;

public record NaverBookSearchResponse(
        String title,
        String author,
        String description,
        String publisher,
        LocalDate publishedDate,
        String isbn,
        String thumbnailImage
) {}