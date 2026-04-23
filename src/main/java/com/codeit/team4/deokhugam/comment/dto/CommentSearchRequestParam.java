package com.codeit.team4.deokhugam.comment.dto;

import com.codeit.team4.deokhugam.global.response.SortDirection;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "댓글 목록 조회 요청")
public record CommentSearchRequestParam(

        @Schema(description = "리뷰 ID", required = true)
        @NotNull(message = "리뷰 ID는 필수입니다.")
        UUID reviewId,

        @Schema(description = "정렬 방향 (ASC | DESC)", defaultValue = "DESC")
        SortDirection direction,

        @Schema(description = "커서 페이지네이션 커서")
        String cursor,

        @Schema(description = "보조 커서 (createdAt)")
        Instant after,

        @Schema(description = "페이지 크기", defaultValue = "50")
        @Min(1) @Max(100)
        Integer limit
) {
    private static final int DEFAULT_LIMIT = 50;

    public CommentSearchRequestParam {
        if (direction == null) direction = SortDirection.DESC;
        if (limit == null) limit = DEFAULT_LIMIT;
    }
}