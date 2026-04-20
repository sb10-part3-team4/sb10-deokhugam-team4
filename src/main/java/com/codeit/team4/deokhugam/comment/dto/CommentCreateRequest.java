package com.codeit.team4.deokhugam.comment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CommentCreateRequest(

        @Schema(description = "댓글을 작성할 리뷰의 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        @NotNull(message = "리뷰 ID는 필수입니다.")
        UUID reviewId,

        @Schema(description = "댓글 작성자 ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @NotNull(message = "유저 ID는 필수입니다.")
        UUID userId,

        @Schema(description = "댓글 내용", example = "좋은 리뷰 감사합니다.")
        @NotBlank(message = "댓글 내용은 필수입니다.")
        @Size(max = 1000, message = "댓글은 최대 1000자까지 입력 가능합니다.")
        String content
) {

}