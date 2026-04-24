package com.codeit.team4.deokhugam.dashboard.dto;

import com.codeit.team4.deokhugam.dashboard.entity.PeriodType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "파워 유저 응답")
public record PowerUserResponse(

        @Schema(description = "파워 유저 ID")
        UUID id,

        @Schema(description = "사용자 ID")
        UUID userId,

        @Schema(description = "닉네임")
        String nickname,

        @Schema(description = "랭킹 기간")
        PeriodType period,

        @Schema(description = "순위")
        int rank,

        @Schema(description = "점수")
        BigDecimal score,

        @Schema(description = "리뷰 점수 합계")
        BigDecimal reviewScoreSum,

        @Schema(description = "좋아요 수")
        int likeCount,

        @Schema(description = "댓글 수")
        int commentCount,

        @Schema(description = "생성 일시")
        Instant createdAt
) {

}
