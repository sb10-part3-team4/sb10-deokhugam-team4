package com.codeit.team4.deokhugam.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "리뷰 좋아요 응답")
public record ReviewLikeResponse(

        @Schema(description = "리뷰 ID")
        UUID reviewId,

        @Schema(description = "사용자 ID")
        UUID userId,

        @Schema(description = "좋아요 여부")
        boolean liked
) {

}
