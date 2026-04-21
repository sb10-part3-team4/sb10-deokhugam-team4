package com.codeit.team4.deokhugam.user.controller;

import com.codeit.team4.deokhugam.global.annotation.LoginUser;
import com.codeit.team4.deokhugam.global.dto.DeokhugamUser;
import com.codeit.team4.deokhugam.user.controller.api.UserApi;
import com.codeit.team4.deokhugam.user.dto.*;
import com.codeit.team4.deokhugam.user.service.UserService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody UserRegisterRequest request
    ) {
        log.info("회원가입 요청: email={}", request.email());

        UserResponse response = userService.registerUser(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(
            @Valid @RequestBody UserLoginRequest request
    ) {
        log.info("로그인 요청: email={}", request.email());

        UserResponse response = userService.loginUser(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(
            @PathVariable UUID userId
    ) {
        log.info("유저 조회 요청: userId={}", userId);

        UserResponse response = userService.getUser(userId);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable UUID userId,
            @LoginUser DeokhugamUser loginUser,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        log.info("유저 수정 요청: targetUserId={}, loginUserId={}", userId, loginUser.userId());

        UserResponse response = userService.updateUser(userId, loginUser.userId(), request);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable UUID userId,
            @LoginUser DeokhugamUser loginUser
    ) {
        log.info("유저 논리 삭제 요청: targetUserId={}, loginUserId={}", userId, loginUser.userId());

        userService.softDeleteUser(userId, loginUser.userId());

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}/hard")
    public ResponseEntity<Void> hardDeleteUser(
            @PathVariable UUID userId,
            @LoginUser DeokhugamUser loginUser
    ) {
        log.info("유저 물리 삭제 요청: targetUserId={}, loginUserId={}", userId, loginUser.userId());

        userService.hardDeleteUser(userId, loginUser.userId());

        return ResponseEntity.noContent().build();
    }
}