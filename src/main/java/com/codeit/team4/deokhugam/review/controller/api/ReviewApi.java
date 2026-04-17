package com.codeit.team4.deokhugam.review.controller.api;

import com.codeit.team4.deokhugam.global.error.ErrorResponse;
import com.codeit.team4.deokhugam.review.dto.ReviewCreateRequest;
import com.codeit.team4.deokhugam.review.dto.ReviewResponse;
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

    @Operation(summary = "리뷰 생성", description = "도서에 대한 리뷰를 작성합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "리뷰 생성 성공",
                    content = @Content(schema = @Schema(implementation = ReviewResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 입력",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "도서 또는 사용자를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "이미 리뷰를 작성함",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<ReviewResponse> createReview(
            @Parameter(description = "리뷰 생성 요청 정보", required = true)
            @RequestBody ReviewCreateRequest request
    );
}
