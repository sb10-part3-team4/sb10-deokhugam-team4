package com.codeit.team4.deokhugam.global.admin;

import com.codeit.team4.deokhugam.global.error.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "관리자", description = "관리자 API")
public interface AdminApi {

    @Operation(summary = "대시보드 배치 수동 실행", description = "인기 도서/리뷰/파워유저 배치를 수동으로 실행합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "배치 실행 성공"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<Void> runDashboardBatch();

    @Operation(summary = "도서 통계 재계산", description = "리뷰 데이터 기반으로 BookStatistics를 전체 재계산합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "재계산 성공"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<Void> recalculateBookStatistics();
}