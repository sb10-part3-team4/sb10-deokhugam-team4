package com.codeit.team4.deokhugam.dashboard.dto;

import com.codeit.team4.deokhugam.dashboard.entity.PeriodType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "인기 도서 응답")
public record PopularBookResponse(

        @Schema(description = "인기 도서 ID")
        UUID id,

        @Schema(description = "도서 ID")
        UUID bookId,

        @Schema(description = "도서 제목")
        String title,

        @Schema(description = "도서 저자")
        String author,

        @Schema(description = "도서 썸네일 URL")
        String thumbnailUrl,

        @Schema(description = "랭킹 기간")
        PeriodType period,

        @Schema(description = "순위")
        int rank,

        @Schema(description = "점수")
        BigDecimal score,

        @Schema(description = "리뷰 수")
        int reviewCount,

        @Schema(description = "평균 평점")
        BigDecimal rating,

        @Schema(description = "생성 일시")
        Instant createdAt
) {

}
