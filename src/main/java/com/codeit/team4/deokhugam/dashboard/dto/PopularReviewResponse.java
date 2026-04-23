package com.codeit.team4.deokhugam.dashboard.dto;

import com.codeit.team4.deokhugam.dashboard.entity.PeriodType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "인기 리뷰 응답")
public record PopularReviewResponse(

        @Schema(description = "인기 리뷰 ID")
        UUID id,

        @Schema(description = "리뷰 ID")
        UUID reviewId,

        @Schema(description = "도서 ID")
        UUID bookId,

        @Schema(description = "도서 제목")
        String bookTitle,

        @Schema(description = "도서 썸네일 URL")
        String bookThumbnailUrl,

        @Schema(description = "사용자 ID")
        UUID userId,

        @Schema(description = "사용자 닉네임")
        String userNickname,

        @Schema(description = "리뷰 내용")
        String reviewContent,

        @Schema(description = "리뷰 평점")
        int reviewRating,

        @Schema(description = "랭킹 기간")
        PeriodType period,

        @Schema(description = "순위")
        int rank,

        @Schema(description = "점수")
        BigDecimal score,

        @Schema(description = "좋아요 수")
        int likeCount,

        @Schema(description = "댓글 수")
        int commentCount,

        @Schema(description = "생성 일시")
        Instant createdAt
) {

}
