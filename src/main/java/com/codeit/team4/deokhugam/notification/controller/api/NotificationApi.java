package com.codeit.team4.deokhugam.notification.controller.api;

import com.codeit.team4.deokhugam.global.annotation.LoginUser;
import com.codeit.team4.deokhugam.global.dto.DeokhugamUser;
import com.codeit.team4.deokhugam.global.error.ErrorResponse;
import com.codeit.team4.deokhugam.global.response.PageResponse;
import com.codeit.team4.deokhugam.notification.dto.NotificationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "알림 관리", description = "알림 관련 API")
public interface NotificationApi {

    @Operation(summary = "알림 목록 조회", description = "사용자의 알림 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "알림 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자 정보 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    PageResponse<NotificationResponse> getNotifications(

            @LoginUser DeokhugamUser loginUser,

            @Parameter(description = "커서")
            @RequestParam(required = false) String cursor,

            @Parameter(description = "보조 커서 (createdAt)")
            @RequestParam(required = false) Instant after,

            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size
    );
}
