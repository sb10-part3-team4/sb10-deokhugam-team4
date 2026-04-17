package com.codeit.team4.deokhugam.comment.service;

import com.codeit.team4.deokhugam.comment.dto.CommentCreateRequest;
import com.codeit.team4.deokhugam.comment.dto.CommentResponse;

public interface CommentService {

    CommentResponse createComment(CommentCreateRequest request);
}
