package com.codeit.team4.deokhugam.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(description = "리뷰 생성 요청")
public record ReviewCreateRequest(

        @Schema(description = "도서 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        @NotNull UUID bookId,

        @Schema(description = "작성자 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        @NotNull UUID userId,

        @Schema(description = "리뷰 내용", example = "정말 유익한 책이었습니다")
        @NotBlank String content,

        @Schema(description = "평점 (1~5)", example = "5")
        @Min(1) @Max(5) int rating
) {

}
