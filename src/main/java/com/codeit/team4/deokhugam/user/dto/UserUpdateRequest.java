package com.codeit.team4.deokhugam.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(

        @Schema(description = "수정할 닉네임 (2~20자)", example = "newNickname")
        @NotBlank
        @Size(min = 2, max = 20)
        String nickname
) {

}
