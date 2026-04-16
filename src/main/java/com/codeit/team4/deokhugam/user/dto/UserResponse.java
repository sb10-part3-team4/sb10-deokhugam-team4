package com.codeit.team4.deokhugam.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

public record UserResponse(

        @Schema(description = "사용자 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,

        @Schema(description = "사용자 이메일", example = "test@test.com")
        String email,

        @Schema(description = "사용자 닉네임", example = "user123")
        String nickname,

        @Schema(description = "생성 일시", example = "2026-04-16T12:00:00Z")
        Instant createdAt
) {

}
