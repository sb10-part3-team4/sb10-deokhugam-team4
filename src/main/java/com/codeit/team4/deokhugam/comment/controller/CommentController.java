package com.codeit.team4.deokhugam.comment.controller;

import com.codeit.team4.deokhugam.comment.controller.api.CommentApi;
import com.codeit.team4.deokhugam.comment.dto.CommentCreateRequest;
import com.codeit.team4.deokhugam.comment.dto.CommentResponse;
import com.codeit.team4.deokhugam.comment.dto.CommentSearchRequestParam;
import com.codeit.team4.deokhugam.comment.dto.CommentUpdateRequest;
import com.codeit.team4.deokhugam.comment.service.query.CommentQueryService;
import com.codeit.team4.deokhugam.comment.service.CommentService;
import com.codeit.team4.deokhugam.global.annotation.LoginUser;
import com.codeit.team4.deokhugam.global.dto.DeokhugamUser;
import com.codeit.team4.deokhugam.global.response.PageResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
    private final CommentQueryService commentQueryService;

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

        log.info("댓글 수정 요청: commentId={}, userId={}", commentId, loginUser.userId());

        return ResponseEntity.ok(
                commentService.updateComment(commentId, loginUser.userId(), request));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> softDeleteComment(
            @PathVariable UUID commentId,
            @LoginUser DeokhugamUser loginUser) {

        log.info("댓글 논리 삭제 요청: commentId={}, userId={}", commentId, loginUser.userId());

        commentService.softDeleteComment(commentId, loginUser.userId());

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{commentId}/hard")
    public ResponseEntity<Void> hardDeleteComment(
            @PathVariable UUID commentId,
            @LoginUser DeokhugamUser loginUser) {

        log.info("댓글 물리 삭제 요청: commentId={}, userId={}", commentId, loginUser.userId());

        commentService.hardDeleteComment(commentId, loginUser.userId());

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<CommentResponse> getComment(
            @PathVariable UUID commentId) {
        log.info("댓글 단건 조회 요청: commentId={}", commentId);
        return ResponseEntity.ok(commentQueryService.getComment(commentId));
    }

    @GetMapping
    public ResponseEntity<PageResponse<CommentResponse>> getComments(
            @Valid @ParameterObject CommentSearchRequestParam param) {
        log.info("댓글 목록 조회 요청: reviewId={}, direction={}, limit={}",
                param.reviewId(), param.direction(), param.limit());
        return ResponseEntity.ok(commentQueryService.getComments(param));
    }
}
