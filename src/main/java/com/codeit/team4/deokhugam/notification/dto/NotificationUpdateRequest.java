package com.codeit.team4.deokhugam.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "알림 상태 변경 요청")
public record NotificationUpdateRequest(

        @Schema(description = "읽음 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        Boolean confirmed

) {}