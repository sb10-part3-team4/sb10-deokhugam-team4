package com.codeit.team4.deokhugam.book.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record BookCreateRequest(
        @NotBlank String title,
        @NotBlank String author,
        @NotBlank String description,
        @NotBlank String publisher,
        @NotNull LocalDate publishedDate,
        String isbn // optional
) {

}
