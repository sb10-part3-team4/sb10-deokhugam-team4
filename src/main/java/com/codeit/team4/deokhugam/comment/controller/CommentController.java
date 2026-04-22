package com.codeit.team4.deokhugam.comment.controller;

import com.codeit.team4.deokhugam.comment.controller.api.CommentApi;
import com.codeit.team4.deokhugam.comment.dto.CommentCreateRequest;
import com.codeit.team4.deokhugam.comment.dto.CommentResponse;
import com.codeit.team4.deokhugam.comment.dto.CommentUpdateRequest;
import com.codeit.team4.deokhugam.comment.service.CommentService;
import com.codeit.team4.deokhugam.global.annotation.LoginUser;
import com.codeit.team4.deokhugam.global.dto.DeokhugamUser;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comments")
public class CommentController implements CommentApi {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponse> createComment(
            @Valid @RequestBody CommentCreateRequest request) {

        log.info("댓글 등록 요청: reviewId={}, userId={}, content={}", request.reviewId(),
                request.userId(), request.content());

        CommentResponse response = commentService.createComment(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable UUID commentId,
            @LoginUser DeokhugamUser loginUser,
            @Valid @RequestBody CommentUpdateRequest request) {

        log.info("댓글 수정 요청: commentId={}  userId={}", commentId, loginUser.userId());

        return ResponseEntity.ok(commentService.updateComment(commentId, loginUser.userId(), request));
    }

    @Override
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> softDeleteComment(
            @PathVariable UUID commentId,
            @LoginUser DeokhugamUser loginUser) {

        commentService.softDeleteComment(commentId, loginUser.userId());

        return ResponseEntity.noContent().build();
    }
}
