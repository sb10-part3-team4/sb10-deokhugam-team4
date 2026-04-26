package com.codeit.team4.deokhugam.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.codeit.team4.deokhugam.dashboard.entity.PeriodType;
import com.codeit.team4.deokhugam.global.error.BusinessException;
import com.codeit.team4.deokhugam.global.error.ErrorCode;
import com.codeit.team4.deokhugam.global.response.PageResponse;
import com.codeit.team4.deokhugam.notification.dto.NotificationResponse;
import com.codeit.team4.deokhugam.notification.entity.Notification;
import com.codeit.team4.deokhugam.notification.model.NotificationModel;
import com.codeit.team4.deokhugam.notification.repository.NotificationRepository;
import com.codeit.team4.deokhugam.review.entity.Review;
import com.codeit.team4.deokhugam.review.repository.ReviewRepository;
import com.codeit.team4.deokhugam.user.entity.User;
import com.codeit.team4.deokhugam.user.service.UserService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @InjectMocks
    NotificationService notificationService;

    @Mock
    NotificationQueryService notificationQueryService;

    @Mock
    NotificationRepository notificationRepository;

    @Mock
    ReviewRepository reviewRepository;

    @Mock
    UserService userService;

    @Test
    @DisplayName("좋아요 알림 생성 성공")
    void createLikeNotification_success() {
        // given
        UUID receiverId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        assertThat(receiverId).isNotEqualTo(actorId);

        Review review = mock(Review.class);
        given(review.getContent()).willReturn("review content");

        User user = mock(User.class);
        given(user.getNickname()).willReturn("kim");

        given(userService.findById(actorId)).willReturn(user);

        given(reviewRepository.findByIdAndDeletedAtIsNull(reviewId))
                .willReturn(Optional.of(review));

        // when
        notificationService.createLikeNotification(receiverId, reviewId, actorId);

        // then
        then(notificationRepository).should().save(any(Notification.class));
    }

    @Test
    @DisplayName("댓글 알림 생성 성공")
    void createCommentNotification_success() {
        // given
        UUID receiverId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        assertThat(receiverId).isNotEqualTo(actorId);

        Review review = mock(Review.class);
        given(review.getContent()).willReturn("review content");

        User actor = mock(User.class);
        given(actor.getNickname()).willReturn("kim");

        given(userService.findById(actorId)).willReturn(actor);
        given(reviewRepository.findByIdAndDeletedAtIsNull(reviewId))
                .willReturn(Optional.of(review));

        // when
        notificationService.createCommentNotification(receiverId, reviewId, actorId);

        // then
        then(notificationRepository).should().save(any(Notification.class));
    }

    @Test
    @DisplayName("랭크 알림 생성 성공")
    void createRankNotification_success() {
        // given
        UUID receiverId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        Review review = mock(Review.class);
        given(review.getContent()).willReturn("content");

        given(reviewRepository.findByIdAndDeletedAtIsNull(reviewId))
                .willReturn(Optional.of(review));

        // when
        notificationService.createRankNotification(
                receiverId,
                reviewId,
                PeriodType.DAILY,
                1
        );

        // then
        then(notificationRepository).should().save(any(Notification.class));
    }

    @Test
    @DisplayName("리뷰가 존재하지 않으면 좋아요 알림 생성 실패")
    void createLikeNotification_whenReviewNotFound_thenThrow() {
        // given
        UUID receiverId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        assertThat(receiverId).isNotEqualTo(actorId);

        User actor = mock(User.class);
        given(actor.getNickname()).willReturn("kim");

        given(userService.findById(actorId)).willReturn(actor);
        given(reviewRepository.findByIdAndDeletedAtIsNull(reviewId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                notificationService.createLikeNotification(receiverId, reviewId, actorId)
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("reviewId=" + reviewId);
    }

    @Test
    @DisplayName("댓글 알림 저장 실패 시 예외 발생")
    void createCommentNotification_whenSaveFails_thenThrow() {
        UUID receiverId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        Review review = mock(Review.class);
        given(review.getContent()).willReturn("content");

        User actor = mock(User.class);
        given(actor.getNickname()).willReturn("kim");

        given(userService.findById(actorId)).willReturn(actor);
        given(reviewRepository.findByIdAndDeletedAtIsNull(reviewId))
                .willReturn(Optional.of(review));

        willThrow(new RuntimeException("DB error"))
                .given(notificationRepository)
                .save(any());

        assertThatThrownBy(() ->
                notificationService.createCommentNotification(receiverId, reviewId, actorId)
        )
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB error");
    }

    @Test
    @DisplayName("랭크 알림 저장 실패 시 예외 발생")
    void createRankNotification_whenSaveFails_thenThrow() {
        UUID receiverId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        Review review = mock(Review.class);
        given(review.getContent()).willReturn("content");

        given(reviewRepository.findByIdAndDeletedAtIsNull(reviewId))
                .willReturn(Optional.of(review));

        willThrow(new RuntimeException("DB error"))
                .given(notificationRepository)
                .save(any());

        assertThatThrownBy(() ->
                notificationService.createRankNotification(
                        receiverId,
                        reviewId,
                        PeriodType.DAILY,
                        1
                )
        )
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB error");
    }

    @Test
    @DisplayName("알림 저장 중 DB 오류 발생 시 좋아요 알림 생성 실패")
    void createLikeNotification_whenSaveFails_thenThrow() {
        // given
        UUID receiverId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        Review review = mock(Review.class);
        given(review.getContent()).willReturn("content");

        User actor = mock(User.class);
        given(actor.getNickname()).willReturn("kim");

        given(userService.findById(actorId)).willReturn(actor);
        given(reviewRepository.findByIdAndDeletedAtIsNull(reviewId))
                .willReturn(Optional.of(review));

        willThrow(new RuntimeException("DB error"))
                .given(notificationRepository)
                .save(any());

        // when & then
        assertThatThrownBy(() ->
                notificationService.createLikeNotification(receiverId, reviewId, actorId)
        )
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB error");
    }

    @Test
    @DisplayName("읽지 않은 알림을 읽음 처리하여 성공")
    void markAsRead_whenUnread_thenSuccess() {
        // given
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();

        Notification notification = new Notification(
                userId,
                UUID.randomUUID(),
                "content",
                "message"
        );

        given(notificationRepository.findById(notificationId))
                .willReturn(Optional.of(notification));

        // when
        NotificationResponse result =
                notificationService.markAsRead(notificationId, userId);

        // then
        assertThat(result.confirmed()).isTrue();
    }

    @Test
    @DisplayName("이미 읽은 알림은 추가 처리 없이 성공")
    void markAsRead_whenAlreadyRead_thenSuccess() {
        // given
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();

        Notification notification = mock(Notification.class);

        given(notificationRepository.findById(notificationId))
                .willReturn(Optional.of(notification));

        given(notification.getUserId()).willReturn(userId);
        given(notification.isConfirmed()).willReturn(true);

        // when
        notificationService.markAsRead(notificationId, userId);

        // then
        then(notification).should(never()).markAsRead();
    }

    @Test
    @DisplayName("알림이 존재하지 않으면 조회 실패")
    void markAsRead_whenNotFound_thenFail() {
        // given
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();

        given(notificationRepository.findById(notificationId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                notificationService.markAsRead(notificationId, userId)
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("notificationId=" + notificationId);
    }

    @Test
    @DisplayName("다른 사용자의 알림이면 읽음 처리 실패")
    void markAsRead_whenNotOwner_thenFail() {
        // given
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();

        Notification notification = mock(Notification.class);

        given(notificationRepository.findById(notificationId))
                .willReturn(Optional.of(notification));

        given(notification.getUserId()).willReturn(otherUserId);

        // when & then
        assertThatThrownBy(() ->
                notificationService.markAsRead(notificationId, userId)
        )
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.USER_FORBIDDEN);
    }

    @Test
    @DisplayName("읽지 않은 알림 여러 개를 모두 읽음 처리하여 성공")
    void markAllAsRead_whenUnreadExists_thenSuccess() {
        // given
        UUID userId = UUID.randomUUID();

        Notification n1 = mock(Notification.class);
        Notification n2 = mock(Notification.class);

        given(notificationRepository.findByUserIdAndConfirmedFalse(userId))
                .willReturn(List.of(n1, n2));

        // when
        notificationService.markAllAsRead(userId);

        // then
        then(n1).should().markAsRead();
        then(n2).should().markAsRead();
        then(notificationRepository).should()
                .findByUserIdAndConfirmedFalse(userId);
    }

    @Test
    @DisplayName("읽지 않은 알림이 없으면 조회만 수행되고 아무 동작 없이 성공")
    void markAllAsRead_whenNoUnread_thenSuccess() {
        // given
        UUID userId = UUID.randomUUID();

        given(notificationRepository.findByUserIdAndConfirmedFalse(userId))
                .willReturn(List.of());

        // when
        notificationService.markAllAsRead(userId);

        // then
        then(notificationRepository).should(times(1))
                .findByUserIdAndConfirmedFalse(userId);
    }

    @Test
    @DisplayName("여러 알림 중 일부 처리 중 오류 발생 시 전체 처리 실패")
    void markAllAsRead_whenPartialFailure_thenFail() {
        // given
        UUID userId = UUID.randomUUID();

        Notification n1 = mock(Notification.class);
        Notification n2 = mock(Notification.class);

        given(notificationRepository.findByUserIdAndConfirmedFalse(userId))
                .willReturn(List.of(n1, n2));

        willThrow(new RuntimeException("중간 실패"))
                .given(n2).markAsRead();

        // when & then
        assertThatThrownBy(() ->
                notificationService.markAllAsRead(userId)
        ).isInstanceOf(RuntimeException.class);

        then(n1).should().markAsRead();
        then(n2).should().markAsRead();
    }

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

    @Test
    @DisplayName("7일 이전 읽음 알림을 삭제해서 성공")
    void deleteExpiredNotifications_success() {

        // given
        given(notificationRepository.deleteOldReadNotifications(any()))
                .willReturn(3);

        // when
        notificationService.deleteExpiredNotifications();

        // then
        then(notificationRepository)
                .should()
                .deleteOldReadNotifications(any());
    }

    @Test
    @DisplayName("삭제 대상이 없으면 0건으로 처리해서 성공")
    void deleteExpiredNotifications_whenNothingToDelete_success() {

        // given
        given(notificationRepository.deleteOldReadNotifications(any()))
                .willReturn(0);

        // when
        notificationService.deleteExpiredNotifications();

        // then
        then(notificationRepository)
                .should()
                .deleteOldReadNotifications(any());
    }

    @Test
    @DisplayName("삭제 중 DB 오류로 인해 예외 발생")
    void deleteExpiredNotifications_whenRepositoryThrowsException_thenFail() {

        // given
        given(notificationRepository.deleteOldReadNotifications(any()))
                .willThrow(new RuntimeException("DB error"));

        // when & then
        assertThatThrownBy(() ->
                notificationService.deleteExpiredNotifications()
        )
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB error");
    }
}