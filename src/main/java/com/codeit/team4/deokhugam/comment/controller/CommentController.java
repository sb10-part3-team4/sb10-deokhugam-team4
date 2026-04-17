package com.codeit.team4.deokhugam.comment.controller;

import com.codeit.team4.deokhugam.comment.controller.api.CommentApi;
import com.codeit.team4.deokhugam.comment.dto.CommentCreateRequest;
import com.codeit.team4.deokhugam.comment.dto.CommentResponse;
import com.codeit.team4.deokhugam.comment.service.CommentService;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CommentController implements CommentApi {

    private final CommentService commentService;

    @Override
    public ResponseEntity createComment(CommentCreateRequest request) {
        CommentResponse response = commentService.createComment(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
