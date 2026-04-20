package com.codeit.team4.deokhugam.global.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

@Schema(description = "커서 기반 페이지 응답")
public record PageResponse<T>(

        @Schema(description = "조회 결과 목록")
        List<T> content,

        @Schema(description = "다음 페이지 커서")
        String nextCursor,

        @Schema(description = "다음 페이지 기준 시간")
        Instant nextAfter,

        @Schema(description = "페이지 크기")
        int size,

        @Schema(description = "전체 요소 수")
        long totalElements,

        @Schema(description = "다음 페이지 존재 여부")
        boolean hasNext
) {

}
