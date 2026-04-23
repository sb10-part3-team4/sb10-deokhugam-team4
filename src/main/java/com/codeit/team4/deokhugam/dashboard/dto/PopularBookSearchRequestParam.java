package com.codeit.team4.deokhugam.dashboard.dto;

import com.codeit.team4.deokhugam.dashboard.entity.PeriodType;
import com.codeit.team4.deokhugam.global.error.BusinessException;
import com.codeit.team4.deokhugam.global.error.ErrorCode;
import com.codeit.team4.deokhugam.global.response.SortDirection;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.Instant;

@Schema(description = "인기 도서 검색 요청")
public record PopularBookSearchRequestParam(

        @Schema(description = "랭킹 기간", example = "DAILY", defaultValue = "DAILY")
        PeriodType period,

        @Schema(description = "정렬 방향", example = "DESC", defaultValue = "ASC")
        SortDirection direction,

        @Schema(description = "커서 페이지네이션 커서")
        String cursor,

        @Schema(description = "보조 커서(createdAt)")
        Instant after,

        @Schema(description = "페이지 크기", example = "50", defaultValue = "50")
        @Min(1)
        @Max(100)
        Integer limit
) {

    private static final int DEFAULT_LIMIT = 50;

    public PopularBookSearchRequestParam {
        if (period == null) {
            period = PeriodType.DAILY;
        }
        if (direction == null) {
            direction = SortDirection.ASC;
        }
        if (limit == null) {
            limit = DEFAULT_LIMIT;
        }
    }

    public Integer cursorAsInteger() {
        if (cursor == null) {
            return null;
        }
        try {
            return Integer.parseInt(cursor);
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "cursor는 정수여야 합니다: " + cursor);
        }
    }
}
