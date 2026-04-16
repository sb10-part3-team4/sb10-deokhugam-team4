package com.codeit.team4.deokhugam.user.dto;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(

        UUID id,
        String email,
        String nickname,
        Instant createdAt
) {

}
