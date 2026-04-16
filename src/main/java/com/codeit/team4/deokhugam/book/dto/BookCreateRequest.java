package com.codeit.team4.deokhugam.book.dto;

import java.time.LocalDate;

public record BookCreateRequest(
        String title,
        String author,
        String description,
        String publisher,
        LocalDate publishedDate,
        String isbn
) {

}
