package com.codeit.team4.deokhugam.notification.controller;

import com.codeit.team4.deokhugam.global.config.AppProperties;
import com.codeit.team4.deokhugam.global.response.PageResponse;
import com.codeit.team4.deokhugam.notification.dto.NotificationResponse;
import com.codeit.team4.deokhugam.notification.service.NotificationService;
import com.codeit.team4.deokhugam.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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