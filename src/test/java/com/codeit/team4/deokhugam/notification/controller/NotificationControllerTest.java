package com.codeit.team4.deokhugam.notification.controller;

import com.codeit.team4.deokhugam.global.config.AppProperties;
import com.codeit.team4.deokhugam.global.error.BusinessException;
import com.codeit.team4.deokhugam.global.error.ErrorCode;
import com.codeit.team4.deokhugam.global.response.PageResponse;
import com.codeit.team4.deokhugam.notification.dto.NotificationResponse;
import com.codeit.team4.deokhugam.notification.service.NotificationService;
import com.codeit.team4.deokhugam.user.service.UserService;

import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@Import(AppProperties.class)
@ActiveProfiles("test")
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private NotificationService notificationService;

    private static final String USER_HEADER = "Deokhugam-Request-User-ID";

    @Test
    @DisplayName("알림 읽음 처리 요청이 정상 처리되어 성공")
    void markAsRead_success() throws Exception {

        // given
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();

        NotificationResponse response = new NotificationResponse(
                notificationId,
                userId,
                UUID.randomUUID(),
                "review content",
                "message",
                true,
                Instant.now(),
                Instant.now()
        );

        given(notificationService.markAsRead(notificationId, userId))
                .willReturn(response);

        // when & then
        mockMvc.perform(patch("/api/notifications/{notificationId}", notificationId)
                        .header(USER_HEADER, userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmed").value(true))
                .andExpect(jsonPath("$.message").value("message"));

        then(notificationService)
                .should()
                .markAsRead(notificationId, userId);
    }

    @Test
    @DisplayName("다른 사용자의 알림이면 읽음 처리 실패")
    void markAsRead_forbidden_fail() throws Exception {

        // given
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();

        given(notificationService.markAsRead(notificationId, userId))
                .willThrow(new BusinessException(ErrorCode.USER_FORBIDDEN));

        // when & then
        mockMvc.perform(patch("/api/notifications/{notificationId}", notificationId)
                        .header(USER_HEADER, userId.toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("알림이 존재하지 않으면 읽음 처리 실패")
    void markAsRead_notFound_fail() throws Exception {

        // given
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();

        given(notificationService.markAsRead(notificationId, userId))
                .willThrow(new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));

        // when & then
        mockMvc.perform(patch("/api/notifications/{notificationId}", notificationId)
                        .header(USER_HEADER, userId.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("모든 알림 읽음 처리 요청이 정상 처리되어 성공")
    void markAllAsRead_success() throws Exception {

        // given
        UUID userId = UUID.randomUUID();

        // when & then
        mockMvc.perform(patch("/api/notifications/read-all")
                        .header(USER_HEADER, userId.toString()))
                .andExpect(status().isNoContent());

        then(notificationService)
                .should()
                .markAllAsRead(userId);
    }

    @Test
    @DisplayName("사용자가 없으면 전체 읽음 처리 실패")
    void markAllAsRead_userNotFound_fail() throws Exception {

        // given
        UUID userId = UUID.randomUUID();

        willThrow(new BusinessException(ErrorCode.USER_NOT_FOUND))
                .given(userService)
                .findById(userId);

        // when & then
        mockMvc.perform(patch("/api/notifications/read-all")
                        .header(USER_HEADER, userId.toString()))
                .andExpect(status().isNotFound());

        then(notificationService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("로그인 사용자의 알림 목록 조회 요청이 정상 처리되어서 알림 목록 조회 성공")
    void getNotifications_success() throws Exception {

        // given
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();

        NotificationResponse response = new NotificationResponse(
                UUID.randomUUID(),
                userId,
                UUID.randomUUID(),
                "review content",
                "message",
                false,
                now,
                now
        );

        PageResponse<NotificationResponse> pageResponse =
                new PageResponse<>(
                        List.of(response),
                        null,
                        null,
                        10,
                        null,
                        false
                );

        given(notificationService.getNotifications(
                eq(userId),
                any(),
                any(),
                anyInt()
        )).willReturn(pageResponse);

        // when
        var result = mockMvc.perform(get("/api/notifications")
                .header(USER_HEADER, userId.toString())
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].message").value("message"))
                .andExpect(jsonPath("$.content[0].reviewContent").value("review content"))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    @DisplayName("커서 기반 요청이 정상 처리되어서 다음 페이지 알림 목록 조회 성공")
    void getNotifications_cursor_success() throws Exception {

        // given
        UUID userId = UUID.randomUUID();
        Instant after = Instant.now();

        NotificationResponse response = new NotificationResponse(
                UUID.randomUUID(),
                userId,
                UUID.randomUUID(),
                "review",
                "msg",
                false,
                after,
                after
        );

        PageResponse<NotificationResponse> pageResponse =
                new PageResponse<>(
                        List.of(response),
                        "cursor-id",
                        after,
                        5,
                        null,
                        true
                );

        given(notificationService.getNotifications(
                eq(userId),
                eq("cursor-id"),
                any(),
                eq(5)
        )).willReturn(pageResponse);

        // when
        var result = mockMvc.perform(get("/api/notifications")
                .header(USER_HEADER, userId.toString())
                .param("cursor", "cursor-id")
                .param("after", after.toString())
                .param("size", "5")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.nextCursor").value("cursor-id"));
    }

    @Test
    @DisplayName("헤더 누락으로 인해 알림 목록 조회 실패")
    void getNotifications_missingHeader_fail() throws Exception {

        // when
        var result = mockMvc.perform(get("/api/notifications")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("MISSING_HEADER"));
    }
}