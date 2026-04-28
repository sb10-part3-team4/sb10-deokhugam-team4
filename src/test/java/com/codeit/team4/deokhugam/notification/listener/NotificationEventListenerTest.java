package com.codeit.team4.deokhugam.notification.listener;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

import com.codeit.team4.deokhugam.notification.event.CommentEvent;
import com.codeit.team4.deokhugam.notification.event.LikeEvent;
import com.codeit.team4.deokhugam.notification.event.ReviewRankedEvent;
import com.codeit.team4.deokhugam.notification.service.NotificationService;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationEventListenerTest {

    @InjectMocks
    private NotificationEventListener listener;

    @Mock
    private NotificationService notificationService;

    @Test
    @DisplayName("좋아요 알림 생성 이벤트 처리 성공")
    void handleLikeCreated_success() {
        UUID receiverId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        LikeEvent event = new LikeEvent(reviewId, receiverId, actorId);

        listener.handleLikeCreated(event);

        verify(notificationService).createLikeNotification(receiverId, reviewId, actorId);
    }

    @Test
    @DisplayName("receiverId가 null일 때 좋아요 알림 생성 스킵 성공")
    void handleLikeCreated_receiverNull_skip() {
        UUID reviewId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        LikeEvent event = new LikeEvent(reviewId, null, actorId);

        listener.handleLikeCreated(event);

        verify(notificationService, never()).createLikeNotification(any(), any(), any());
    }

    @Test
    @DisplayName("본인 좋아요일 때 알림 생성 스킵 성공")
    void handleLikeCreated_selfLike_skip() {
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        LikeEvent event = new LikeEvent(reviewId, userId, userId);

        listener.handleLikeCreated(event);

        verify(notificationService, never()).createLikeNotification(any(), any(), any());
    }

    @Test
    @DisplayName("좋아요 알림 생성 중 예외 발생")
    void handleLikeCreated_exception() {
        UUID receiverId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        LikeEvent event = new LikeEvent(reviewId, receiverId, actorId);

        doThrow(new RuntimeException("실패"))
                .when(notificationService)
                .createLikeNotification(receiverId, reviewId, actorId);

        assertThatCode(() -> listener.handleLikeCreated(event))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("댓글 알림 생성 이벤트 처리 성공")
    void handleCommentCreated_success() {
        UUID receiverId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        CommentEvent event = new CommentEvent(reviewId, receiverId, actorId);

        listener.handleCommentCreated(event);

        verify(notificationService).createCommentNotification(receiverId, reviewId, actorId);
    }

    @Test
    @DisplayName("receiverId가 null일 때 댓글 알림 생성 스킵 성공")
    void handleCommentCreated_receiverNull_skip() {
        UUID reviewId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        CommentEvent event = new CommentEvent(reviewId, null, actorId);

        listener.handleCommentCreated(event);

        verify(notificationService, never()).createCommentNotification(any(), any(), any());
    }

    @Test
    @DisplayName("본인 댓글일 때 알림 생성 스킵 성공")
    void handleCommentCreated_self_skip() {
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        CommentEvent event = new CommentEvent(reviewId, userId, userId);

        listener.handleCommentCreated(event);

        verify(notificationService, never()).createCommentNotification(any(), any(), any());
    }

    @Test
    @DisplayName("랭크 알림 생성 이벤트 처리 성공")
    void handleReviewRanked_success() {
        UUID receiverId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        ReviewRankedEvent event =
                new ReviewRankedEvent(reviewId, receiverId, null, 1, null);

        listener.handleReviewRanked(event);

        verify(notificationService)
                .createRankNotification(receiverId, reviewId, null, 1);
    }

    @Test
    @DisplayName("receiverId가 null일 때 랭크 알림 생성 스킵 성공")
    void handleReviewRanked_receiverNull_skip() {
        UUID reviewId = UUID.randomUUID();

        ReviewRankedEvent event =
                new ReviewRankedEvent(reviewId, null, null, 1, null);

        listener.handleReviewRanked(event);

        verify(notificationService, never())
                .createRankNotification(any(), any(), any(), anyInt());
    }

    @Test
    @DisplayName("LikeEvent recover 시 예외 전파 없이 종료 성공")
    void recover_like_success() {
        LikeEvent event = new LikeEvent(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        assertThatCode(() -> listener.recover(new RuntimeException("실패"), event))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("CommentEvent recover 시 예외 전파 없이 종료 성공")
    void recover_comment_success() {
        CommentEvent event = new CommentEvent(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        assertThatCode(() -> listener.recover(new RuntimeException("실패"), event))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("ReviewRankedEvent recover 시 예외 전파 없이 종료 성공")
    void recover_rank_success() {
        ReviewRankedEvent event =
                new ReviewRankedEvent(UUID.randomUUID(), UUID.randomUUID(), null, 1, null);

        assertThatCode(() -> listener.recover(new RuntimeException("실패"), event))
                .doesNotThrowAnyException();
    }
}