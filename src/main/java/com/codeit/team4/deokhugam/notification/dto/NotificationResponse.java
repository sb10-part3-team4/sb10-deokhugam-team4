package com.codeit.team4.deokhugam.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "알림 응답 정보")
public record NotificationResponse(

        @Schema(description = "알림 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID id,

        @Schema(description = "알림 수신 사용자 ID", example = "d290f1ee-6c54-4b01-90e6-d701748f0851")
        UUID userId,

        @Schema(description = "관련 리뷰 ID", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
        UUID reviewId,

        @Schema(description = "리뷰 내용", example = "이 책 정말 재밌어요!")
        String reviewContent,

        @Schema(description = "알림 메시지", example = "회원님의 리뷰에 좋아요가 추가되었습니다.")
        String message,

        @Schema(description = "읽음 여부", example = "false")
        boolean confirmed,

        @Schema(description = "생성 시간", example = "2026-04-22T10:15:30Z")
        Instant createdAt,

        @Schema(description = "수정 시간", example = "2026-04-22T10:15:30Z")
        Instant updatedAt

) {}