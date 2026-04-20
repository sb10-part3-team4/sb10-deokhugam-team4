package com.codeit.team4.deokhugam.comment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "댓글 응답 정보")
public record CommentResponse(

        @Schema(description = "생성된 댓글의 고유 ID", example = "987e6543-e21b-34d3-b456-426614174111")
        UUID id,

        @Schema(description = "댓글 내용", example = "좋은 리뷰 감사합니다.")
        String content,

        @Schema(description = "작성자 ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID userId,

        @Schema(description = "리뷰 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID reviewId,

        @Schema(description = "작성자 닉네임")
        String userNickname,

        @Schema(description = "생성 일시")
        LocalDateTime createdAt,

        @Schema(description = "수정 일시")
        LocalDateTime updatedAt

) {

}
