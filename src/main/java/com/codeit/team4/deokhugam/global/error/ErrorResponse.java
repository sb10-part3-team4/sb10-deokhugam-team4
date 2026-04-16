package com.codeit.team4.deokhugam.global.error;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "에러 응답")
public record ErrorResponse(

        @Schema(description = "에러 코드", example = "INVALID_INPUT")
        String errorCode,

        @Schema(description = "에러 메시지", example = "잘못된 입력입니다")
        String message
) {

}
