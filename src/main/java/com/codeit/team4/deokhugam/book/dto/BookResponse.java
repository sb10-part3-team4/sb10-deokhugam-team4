package com.codeit.team4.deokhugam.book.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "도서 응답")
public record BookResponse(
        @Schema(description = "도서 ID")
        UUID id,

        @Schema(description = "도서 제목", example = "클린 코드")
        String title,

        @Schema(description = "저자 이름", example = "로버트 마틴")
        String author,

        @Schema(description = "도서 소개", example = "좋은 코드를 작성하는 방법")
        String description,

        @Schema(description = "출판사", example = "인사이트")
        String publisher,

        @Schema(description = "출간일", example = "2023-01-01")
        LocalDate publishedDate,

        @Schema(description = "ISBN", example = "9788991995001")
        String isbn,

        @Schema(description = "썸네일 이미지 (Base64 인코딩)")
        String thumbnailUrl,

        @Schema(description = "리뷰 수")
        int reviewCount,

        @Schema(description = "평점")
        BigDecimal rating,

        @Schema(description = "생성일시")
        Instant createdAt,

        @Schema(description = "수정일시")
        Instant updatedAt
) {}
