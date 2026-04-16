package com.codeit.team4.deokhugam.global.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Health Check", description = "서버 상태 확인 API")
public interface HealthCheckApi {

    @Operation(summary = "서버 헬스 체크", description = "서버가 정상적으로 구동 중인지 확인하고 로그를 남깁니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정상 작동 상태"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    ResponseEntity<String> checkHealth();
}