package com.codeit.team4.deokhugam.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codeit.team4.deokhugam.global.config.AppProperties;
import com.codeit.team4.deokhugam.global.error.BusinessException;
import com.codeit.team4.deokhugam.global.error.ErrorCode;
import com.codeit.team4.deokhugam.user.dto.UserResponse;
import com.codeit.team4.deokhugam.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@Import(AppProperties.class)
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private static final String USER_ID_HEADER = "Deokhugam-Request-User-ID";

    @Test
    @DisplayName("회원가입 성공")
    void register_success() throws Exception {

        // given
        UserResponse response = new UserResponse(
                UUID.randomUUID(),
                "test@test.com",
                "user1",
                Instant.now()
        );

        given(userService.registerUser(any())).willReturn(response);

        var request = java.util.Map.of(
                "email", "test@test.com",
                "nickname", "user1",
                "password", "password1!"
        );

        // when
        var result = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    @DisplayName("이메일이 중복 되어서 회원가입 실패")
    void register_fail_duplicateEmail() throws Exception {

        // given
        given(userService.registerUser(any()))
                .willThrow(new BusinessException(ErrorCode.DUPLICATE_EMAIL));

        var request = java.util.Map.of(
                "email", "test@test.com",
                "nickname", "user1",
                "password", "password1!"
        );

        // when
        var result = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("DUPLICATE_EMAIL"));
    }

    @Test
    @DisplayName("이메일 형식 올바르지 않아서 회원가입 실패")
    void register_fail_invalidEmail() throws Exception {

        // given
        var request = java.util.Map.of(
                "email", "invalid-email",
                "nickname", "user1",
                "password", "password1!"
        );

        // when
        var result = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("필수값 누락으로 회원가입 실패")
    void register_fail_missingField() throws Exception {

        // given
        var request = java.util.Map.of(
                "email", "",
                "nickname", "user1",
                "password", "password1!"
        );

        // when
        var result = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("로그인 성공")
    void login_success() throws Exception {

        // given
        UserResponse response = new UserResponse(
                UUID.randomUUID(),
                "test@test.com",
                "user1",
                Instant.now()
        );

        given(userService.loginUser(any())).willReturn(response);

        var request = java.util.Map.of(
                "email", "test@test.com",
                "password", "password1!"
        );

        // when
        var result = mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@test.com"));
    }

    @Test
    @DisplayName("비밀번호가 올바르지 않아서 로그인 실패")
    void login_fail_wrongPassword() throws Exception {

        // given
        given(userService.loginUser(any()))
                .willThrow(new BusinessException(ErrorCode.INVALID_PASSWORD));

        var request = java.util.Map.of(
                "email", "test@test.com",
                "password", "wrong"
        );

        // when
        var result = mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("INVALID_PASSWORD"));
    }

    @Test
    @DisplayName("사용자가 존재하지 않아서 로그인 실패")
    void login_fail_userNotFound() throws Exception {

        // given
        given(userService.loginUser(any()))
                .willThrow(new BusinessException(ErrorCode.USER_NOT_FOUND));

        var request = java.util.Map.of(
                "email", "test@test.com",
                "password", "wrong"
        );

        // when
        var result = mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.errorCode").value("USER_NOT_FOUND"));
    }

    @Test
    @DisplayName("사용자 정보 수정 성공")
    void updateUser_success() throws Exception {

        // given
        UUID userId = UUID.randomUUID();
        UUID loginUserId = UUID.randomUUID();

        UserResponse response = new UserResponse(
                userId,
                "test@test.com",
                "newNickname",
                Instant.now()
        );

        given(userService.updateUser(eq(userId), any()))
                .willReturn(response);

        var request = java.util.Map.of(
                "nickname", "newNickname"
        );

        // when
        var result = mockMvc.perform(patch("/api/users/{userId}", userId)
                .header(USER_ID_HEADER, loginUserId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.nickname").value("newNickname"));
    }

    @Test
    @DisplayName("헤더 누락 시 사용자 수정 실패")
    void updateUser_missingHeader_fail() throws Exception {

        // given
        UUID userId = UUID.randomUUID();

        var request = java.util.Map.of(
                "nickname", "newNickname"
        );

        // when
        var result = mockMvc.perform(patch("/api/users/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("존재하지 않는 사용자 수정 실패")
    void updateUser_notFound_fail() throws Exception {

        // given
        UUID userId = UUID.randomUUID();
        UUID loginUserId = UUID.randomUUID();

        given(userService.updateUser(eq(userId), any()))
                .willThrow(new BusinessException(ErrorCode.USER_NOT_FOUND));

        var request = java.util.Map.of(
                "nickname", "newNickname"
        );

        // when
        var result = mockMvc.perform(patch("/api/users/{userId}", userId)
                .header(USER_ID_HEADER, loginUserId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("USER_NOT_FOUND"));
    }

    @Test
    @DisplayName("사용자 논리 삭제 성공")
    void deleteUser_success() throws Exception {

        // given
        UUID userId = UUID.randomUUID();
        UUID loginUserId = UUID.randomUUID();

        doNothing().when(userService).deleteUser(userId);

        // when
        var result = mockMvc.perform(delete("/api/users/{userId}", userId)
                .header(USER_ID_HEADER, loginUserId.toString()));

        // then
        result.andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("헤더 누락 시 사용자 삭제 실패")
    void deleteUser_missingHeader_fail() throws Exception {

        // given
        UUID userId = UUID.randomUUID();

        // when
        var result = mockMvc.perform(delete("/api/users/{userId}", userId));

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("MISSING_HEADER"));
    }

    @Test
    @DisplayName("존재하지 않는 사용자 삭제 실패")
    void deleteUser_notFound_fail() throws Exception {

        // given
        UUID userId = UUID.randomUUID();
        UUID loginUserId = UUID.randomUUID();

        doThrow(new BusinessException(ErrorCode.USER_NOT_FOUND))
                .when(userService).deleteUser(userId);

        // when
        var result = mockMvc.perform(delete("/api/users/{userId}", userId)
                .header(USER_ID_HEADER, loginUserId.toString()));

        // then
        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("USER_NOT_FOUND"));
    }

    @Test
    @DisplayName("사용자 물리 삭제 성공")
    void hardDeleteUser_success() throws Exception {

        // given
        UUID userId = UUID.randomUUID();
        UUID loginUserId = UUID.randomUUID();

        doNothing().when(userService).deleteExpiredUsers();

        // when
        var result = mockMvc.perform(delete("/api/users/{userId}/hard", userId)
                .header(USER_ID_HEADER, loginUserId.toString()));

        // then
        result.andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("헤더 누락 시 사용자 물리 삭제 실패")
    void hardDeleteUser_missingHeader_fail() throws Exception {

        // given
        UUID userId = UUID.randomUUID();

        // when
        var result = mockMvc.perform(delete("/api/users/{userId}/hard", userId));

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("MISSING_HEADER"));
    }

    @Test
    @DisplayName("존재하지 않는 사용자 물리 삭제 실패")
    void hardDeleteUser_notFound_fail() throws Exception {

        // given
        UUID userId = UUID.randomUUID();
        UUID loginUserId = UUID.randomUUID();

        doThrow(new BusinessException(ErrorCode.USER_NOT_FOUND))
                .when(userService).deleteExpiredUsers();

        // when
        var result = mockMvc.perform(delete("/api/users/{userId}/hard", userId)
                .header(USER_ID_HEADER, loginUserId.toString()));

        // then
        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("USER_NOT_FOUND"));
    }
}