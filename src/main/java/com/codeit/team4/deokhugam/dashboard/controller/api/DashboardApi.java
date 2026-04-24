package com.codeit.team4.deokhugam.dashboard.controller.api;

import com.codeit.team4.deokhugam.dashboard.dto.DashboardSearchRequestParam;
import com.codeit.team4.deokhugam.dashboard.dto.PopularBookResponse;
import com.codeit.team4.deokhugam.dashboard.dto.PopularReviewResponse;
import com.codeit.team4.deokhugam.dashboard.dto.PowerUserResponse;
import com.codeit.team4.deokhugam.global.error.ErrorResponse;
import com.codeit.team4.deokhugam.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;

@Tag(name = "대시보드", description = "대시보드 관련 API")
public interface DashboardApi {

    @Operation(summary = "인기 도서 목록 조회", description = "기간별 인기 도서 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "인기 도서 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (랭킹 기간 오류, 정렬 방향 오류 등)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<PageResponse<PopularBookResponse>> getPopularBooks(
            @ParameterObject DashboardSearchRequestParam param
    );

    @Operation(summary = "인기 리뷰 목록 조회", description = "기간별 인기 리뷰 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "인기 리뷰 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (랭킹 기간 오류, 정렬 방향 오류 등)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<PageResponse<PopularReviewResponse>> getPopularReviews(
            @ParameterObject DashboardSearchRequestParam param
    );

    @Operation(summary = "파워 유저 목록 조회", description = "기간별 파워 유저 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "파워 유저 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (랭킹 기간 오류, 정렬 방향 오류 등)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<PageResponse<PowerUserResponse>> getPowerUsers(
            @ParameterObject DashboardSearchRequestParam param
    );
}
