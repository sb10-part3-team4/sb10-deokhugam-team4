package com.codeit.team4.deokhugam.comment.service;

import com.codeit.team4.deokhugam.comment.dto.CommentCreateRequest;
import com.codeit.team4.deokhugam.comment.dto.CommentResponse;
import java.util.UUID;

public interface CommentService {

    // userId를 dto에서 입력받게되면 보안 위험이 발생하므로 따로 파라미터 받음
    // reviewId는 resource 식별자이므로 request body에 포함시키지 않음
    CommentResponse createComment(UUID userId, UUID reviewId,CommentCreateRequest request);
}
