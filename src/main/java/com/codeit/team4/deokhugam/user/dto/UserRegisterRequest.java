package com.codeit.team4.deokhugam.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRegisterRequest(

        @Schema(description = "사용자 이메일", example = "test@test.com")
        @NotBlank
        @Email
        String email,

        @Schema(description = "사용자 닉네임 (2~20자)", example = "user1")
        @NotBlank
        @Size(min = 2, max = 20)
        String nickname,

        @Schema(
                description = "비밀번호 (영문, 숫자, 특수문자를 각각 최소 1개 이상 포함, 8~20자)",
                example = "password1!"
        )
        @NotBlank
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$")
        String password
) {

}
