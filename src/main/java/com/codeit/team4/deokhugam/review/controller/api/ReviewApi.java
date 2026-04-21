package com.codeit.team4.deokhugam.review.controller.api;

import com.codeit.team4.deokhugam.global.error.ErrorResponse;
import com.codeit.team4.deokhugam.global.response.PageResponse;
import com.codeit.team4.deokhugam.review.dto.ReviewCreateRequest;
import com.codeit.team4.deokhugam.review.dto.ReviewResponse;
import com.codeit.team4.deokhugam.review.dto.ReviewSearchRequestParam;
import com.codeit.team4.deokhugam.review.dto.ReviewUpdateRequest;
import java.util.UUID;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "리뷰 관리", description = "리뷰 관련 API")
public interface ReviewApi {

    @Operation(summary = "리뷰 목록 조회", description = "검색 조건에 맞는 리뷰 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "리뷰 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (정렬 기준 오류, 페이지네이션 파라미터 오류, 요청자 ID 누락)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<PageResponse<ReviewResponse>> searchReviews(
            @ParameterObject ReviewSearchRequestParam param,
            @Parameter(description = "요청자 ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @RequestHeader("Deokhugam-Request-User-ID") UUID userId
    );

    @Operation(summary = "리뷰 상세 정보 조회", description = "리뷰 ID로 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "리뷰 정보 조회 성공",
                    content = @Content(schema = @Schema(implementation = ReviewResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (요청자 ID 누락)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "리뷰 정보 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<ReviewResponse> getReview(
            @Parameter(description = "리뷰 ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID reviewId,
            @Parameter(description = "요청자 ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @RequestHeader("Deokhugam-Request-User-ID") UUID userId
    );

    @Operation(summary = "리뷰 등록", description = "새로운 리뷰를 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "리뷰 등록 성공",
                    content = @Content(schema = @Schema(implementation = ReviewResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "도서 정보 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "이미 작성된 리뷰 존재",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<ReviewResponse> createReview(
            @Parameter(description = "리뷰 생성 요청 정보", required = true)
            @RequestBody ReviewCreateRequest request
    );

    @Operation(summary = "리뷰 수정", description = "본인이 작성한 리뷰를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "리뷰 수정 성공",
                    content = @Content(schema = @Schema(implementation = ReviewResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "리뷰 수정 권한 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "리뷰 정보 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<ReviewResponse> updateReview(
            @Parameter(description = "리뷰 ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID reviewId,
            @Parameter(description = "요청자 ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @RequestHeader("Deokhugam-Request-User-ID") UUID userId,
            @Parameter(description = "리뷰 수정 요청 정보", required = true)
            @RequestBody ReviewUpdateRequest request
    );

    @Operation(summary = "리뷰 논리 삭제", description = "본인이 작성한 리뷰를 논리적으로 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "리뷰 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (요청자 ID 누락)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "리뷰 삭제 권한 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "리뷰 정보 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<Void> softDeleteReview(
            @Parameter(description = "리뷰 ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID reviewId,
            @Parameter(description = "요청자 ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @RequestHeader("Deokhugam-Request-User-ID") UUID userId
    );

    @Operation(summary = "리뷰 물리 삭제", description = "본인이 작성한 리뷰를 물리적으로 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "리뷰 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (요청자 ID 누락)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "리뷰 삭제 권한 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "리뷰 정보 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<Void> hardDeleteReview(
            @Parameter(description = "리뷰 ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID reviewId,
            @Parameter(description = "요청자 ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @RequestHeader("Deokhugam-Request-User-ID") UUID userId
    );
}
