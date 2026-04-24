package com.codeit.team4.deokhugam.comment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "댓글 수정 요청")
public record CommentUpdateRequest(

        @Schema(description = "댓글 내용", example = "수정된 댓글 내용입니다.")
        @NotBlank(message = "댓글 내용은 필수입니다.")
        @Size(max = 1000, message = "댓글은 최대 1000자까지 입력 가능합니다.")
        String content
) {

}