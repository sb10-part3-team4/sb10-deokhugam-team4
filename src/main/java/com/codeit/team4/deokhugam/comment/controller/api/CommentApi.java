package com.codeit.team4.deokhugam.comment.controller.api;

import com.codeit.team4.deokhugam.comment.dto.CommentCreateRequest;
import com.codeit.team4.deokhugam.comment.dto.CommentResponse;
import com.codeit.team4.deokhugam.comment.dto.CommentUpdateRequest;
import com.codeit.team4.deokhugam.global.error.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "댓글 관리", description = "댓글 관련 API")
public interface CommentApi {

    @Operation(summary = "댓글 등록", description = "새로운 댓글을 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "댓글 등록 성공",
                    content = @Content(schema = @Schema(implementation = CommentResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "리뷰 정보 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<CommentResponse> createComment(@Valid @RequestBody CommentCreateRequest request);

    @Operation(summary = "댓글 수정", description = "본인이 작성한 댓글을 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "댓글 수정 성공",
                    content = @Content(schema = @Schema(implementation = CommentResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패, 요청자 ID 누락)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "댓글 수정 권한 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "댓글 정보 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<CommentResponse> updateComment(
            @Parameter(description = "댓글 ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID commentId,
            @Parameter(description = "요청자 ID", required = true)
            @RequestHeader("Deokhugam-Request-User-ID") UUID userId,
            @Valid @RequestBody CommentUpdateRequest request
    );

}
