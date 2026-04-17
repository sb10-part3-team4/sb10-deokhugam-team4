package com.codeit.team4.deokhugam.comment.controller;

import com.codeit.team4.deokhugam.comment.controller.api.CommentApi;
import com.codeit.team4.deokhugam.comment.dto.CommentCreateRequest;
import com.codeit.team4.deokhugam.comment.service.CommentService;
import java.util.UUID;
import org.springframework.http.ResponseEntity;

public class CommentController implements CommentApi {

    private final CommentService commentService;

    @Override
    public ResponseEntity createComment(UUID reviewId, CommentCreateRequest request){
        return null;
    }

}
