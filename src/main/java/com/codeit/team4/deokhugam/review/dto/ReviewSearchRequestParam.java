package com.codeit.team4.deokhugam.review.dto;

import com.codeit.team4.deokhugam.global.response.SortDirection;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "리뷰 검색 요청")
public record ReviewSearchRequestParam(

        @Schema(description = "작성자 ID (완전 일치)", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID userId,

        @Schema(description = "도서 ID (완전 일치)", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID bookId,

        @Schema(description = "검색 키워드 (작성자 닉네임 | 내용 | 도서제목)", example = "홍길동")
        String keyword,

        @Schema(description = "정렬 기준 (createdAt | rating)", example = "createdAt", defaultValue = "createdAt")
        ReviewOrderBy orderBy,

        @Schema(description = "정렬 방향 (ASC | DESC)", example = "DESC", defaultValue = "DESC")
        SortDirection direction,

        @Schema(description = "커서 페이지네이션 커서")
        String cursor,

        @Schema(description = "보조 커서 (createdAt)")
        Instant after,

        @Schema(description = "페이지 크기", example = "50", defaultValue = "50")
        @Min(1)
        @Max(100)
        int limit,

        @Schema(description = "요청자 ID", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        UUID requestUserId
) {

    public ReviewSearchRequestParam {
        if (orderBy == null) {
            orderBy = ReviewOrderBy.CREATED_AT;
        }
        if (direction == null) {
            direction = SortDirection.DESC;
        }
    }
}
