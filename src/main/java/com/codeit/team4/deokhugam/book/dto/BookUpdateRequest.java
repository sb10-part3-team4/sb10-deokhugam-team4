package com.codeit.team4.deokhugam.book.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Schema(description = "도서 수정 요청")
public record BookUpdateRequest(
        @Schema(description = "도서 제목", example = "클린 코드")
        @NotBlank String title,

        @Schema(description = "저자 이름", example = "로버트 마틴")
        @NotBlank String author,

        @Schema(description = "도서 소개", example = "좋은 코드를 작성하는 방법")
        @NotBlank String description,

        @Schema(description = "출판사", example = "인사이트")
        @NotBlank String publisher,

        @Schema(description = "출간일", example = "2023-01-01")
        @NotNull LocalDate publishedDate
) {}