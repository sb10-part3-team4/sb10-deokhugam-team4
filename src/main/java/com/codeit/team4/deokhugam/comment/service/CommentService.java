package com.codeit.team4.deokhugam.comment.service;

import com.codeit.team4.deokhugam.comment.dto.CommentCreateRequest;
import com.codeit.team4.deokhugam.comment.dto.CommentResponse;
import com.codeit.team4.deokhugam.comment.dto.CommentUpdateRequest;
import java.util.UUID;

public interface CommentService {

    CommentResponse createComment(CommentCreateRequest request);

    CommentResponse updateComment(UUID commentId, UUID userId, CommentUpdateRequest request);
}
