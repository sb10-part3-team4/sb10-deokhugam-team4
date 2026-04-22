package com.codeit.team4.deokhugam.notification.service;

import com.codeit.team4.deokhugam.global.response.PageResponse;
import com.codeit.team4.deokhugam.notification.dto.NotificationResponse;
import com.codeit.team4.deokhugam.notification.model.NotificationModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @InjectMocks
    NotificationService notificationService;

    @Mock
    NotificationQueryService notificationQueryService;

    @Test
    @DisplayName("DTO 변환 및 페이징 응답이 정상적으로 반환되어서 알림 목록 조회 성공")
    void getNotifications_success() {

        // given
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();

        NotificationModel model = new NotificationModel(
                UUID.randomUUID(),
                userId,
                UUID.randomUUID(),
                "review content",
                "message",
                false,
                now,
                now
        );

        given(notificationQueryService.findNotifications(userId, now, 10))
                .willReturn(List.of(model));

        // when
        PageResponse<NotificationResponse> result =
                notificationService.getNotifications(userId, null, now, 10);

        // then
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).message()).isEqualTo("message");
        assertThat(result.content().get(0).reviewContent()).isEqualTo("review content");
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    @DisplayName("다음 페이지가 존재하여서 hasNext가 true이고 cursor가 반환되어서 알림 목록 조회 성공")
    void getNotifications_hasNext_success() {

        // given
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();

        NotificationModel model1 = new NotificationModel(
                UUID.randomUUID(),
                userId,
                UUID.randomUUID(),
                "review1",
                "msg1",
                false,
                now,
                now
        );

        NotificationModel model2 = new NotificationModel(
                UUID.randomUUID(),
                userId,
                UUID.randomUUID(),
                "review2",
                "msg2",
                false,
                now,
                now
        );

        given(notificationQueryService.findNotifications(userId, now, 2))
                .willReturn(List.of(model1, model2, model2));

        // when
        PageResponse<NotificationResponse> result =
                notificationService.getNotifications(userId, null, now, 2);

        // then
        assertThat(result.hasNext()).isTrue();
        assertThat(result.nextCursor()).isEqualTo(model2.id().toString());
        assertThat(result.nextAfter()).isEqualTo(model2.createdAt());
    }

    @Test
    @DisplayName("데이터가 없어서 빈 응답이 반환되어서 알림 목록 조회 성공")
    void getNotifications_empty_success() {

        // given
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();

        given(notificationQueryService.findNotifications(userId, now, 10))
                .willReturn(List.of());

        // when
        PageResponse<NotificationResponse> result =
                notificationService.getNotifications(userId, null, now, 10);

        // then
        assertThat(result.content()).isEmpty();
        assertThat(result.hasNext()).isFalse();
        assertThat(result.nextCursor()).isNull();
        assertThat(result.nextAfter()).isNull();
    }

    @Test
    @DisplayName("요청한 size 값이 PageResponse에 반영되어서 알림 목록 조회 성공")
    void getNotifications_size_success() {

        // given
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();

        NotificationModel model = new NotificationModel(
                UUID.randomUUID(),
                userId,
                UUID.randomUUID(),
                "review",
                "msg",
                false,
                now,
                now
        );

        given(notificationQueryService.findNotifications(userId, now, 5))
                .willReturn(List.of(model));

        // when
        PageResponse<NotificationResponse> result =
                notificationService.getNotifications(userId, null, now, 5);

        // then
        assertThat(result.size()).isEqualTo(5);
    }
}