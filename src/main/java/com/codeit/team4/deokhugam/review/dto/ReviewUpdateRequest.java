package com.codeit.team4.deokhugam.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "리뷰 수정 요청")
public record ReviewUpdateRequest(

        @Schema(description = "리뷰 내용", example = "다시 읽어보니 별로네요")
        @NotBlank String content,

        @Schema(description = "평점 (1~5)", example = "1")
        @Min(1) @Max(5) int rating
) {

}
