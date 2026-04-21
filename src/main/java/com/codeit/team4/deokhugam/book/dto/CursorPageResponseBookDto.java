package com.codeit.team4.deokhugam.book.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

public record CursorPageResponseBookDto(
        @Schema(description = "데이터 목록")
        List<BookResponse> content,

        @Schema(description = "다음 페이지 커서 (제목, 출판일, 평점, 리뷰수)")
        String nextCursor,

        @Schema(description = "다음 페이지 보조 커서", example = "2026-04-20T08:25:13Z")
        Instant nextAfter,

        @Schema(description = "페이지 크기", example = "10")
        int size,

        @Schema(description = "전체 데이터 개수", example = "100")
        long totalElements,

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext
) {
}
