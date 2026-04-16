package com.codeit.team4.deokhugam.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserLoginRequest(

        @Schema(description = "사용자 이메일", example = "test@test.com")
        @NotBlank
        @Email
        String email,

        @Schema(description = "사용자 비밀번호", example = "password1!")
        @NotBlank
        String password
) {

}
