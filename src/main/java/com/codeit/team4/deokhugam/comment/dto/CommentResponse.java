package com.codeit.team4.deokhugam.comment.dto;

import java.util.UUID;

public record CommentResponse (
        UUID id,
        String content,
        UUID userId,
        UUID reviewId
) {
}
