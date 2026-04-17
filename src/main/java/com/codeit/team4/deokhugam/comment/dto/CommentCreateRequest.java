package com.codeit.team4.deokhugam.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CommentCreateRequest(

        @NotNull(message = "리뷰 ID는 필수입니다.")
        UUID reviewId,

        @NotNull(message = "유저 ID는 필수입니다.")
        UUID userId,

        @NotBlank(message = "댓글 내용은 필수입니다.")
        @Size(max = 1000, message = "댓글은 최대 1000자까지 입력 가능합니다.")
        String content
) {

}