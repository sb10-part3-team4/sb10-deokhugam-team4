package com.codeit.team4.deokhugam.notification.listener;

import com.codeit.team4.deokhugam.notification.service.NotificationService;
import com.codeit.team4.deokhugam.notification.event.CommentEvent;
import com.codeit.team4.deokhugam.notification.event.LikeEvent;
import com.codeit.team4.deokhugam.notification.event.ReviewRankedEvent;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationService notificationService;

    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Retryable(
            retryFor = Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 500)
    )
    public void handleLikeCreated(LikeEvent event) {
        log.debug("LikeEvent 수신: {}", event);

        if (event.receiverId() == null || Objects.equals(event.receiverId(), event.actorId())) {
            return;
        }
            notificationService.createLikeNotification(
                    event.receiverId(),
                    event.reviewId(),
                    event.actorId()
            );
    }

    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Retryable(
            retryFor = Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 500)
    )
    public void handleCommentCreated(CommentEvent event) {
        log.debug("CommentEvent 수신: {}", event);

        if (event.receiverId() == null || Objects.equals(event.receiverId(), event.actorId())) {
            return;
        }
            notificationService.createCommentNotification(
                    event.receiverId(),
                    event.reviewId(),
                    event.actorId()
            );
    }

    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Retryable(
            retryFor = Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 500)
    )
    public void handleReviewRanked(ReviewRankedEvent event) {
        log.info("ReviewRankedEvent 수신: {}", event);

        if (event.receiverId() == null) {
            log.warn("알림 생성 스킵 - receiverId가 null입니다. event={}", event);
            return;
        }
            notificationService.createRankNotification(
                    event.receiverId(),
                    event.reviewId(),
                    event.period(),
                    event.rank()
            );
    }

    @Recover
    public void recover(Exception e, LikeEvent event) {
        log.error("NotificationEvent 최종 실패 (Like): reviewId={}, receiverId={}, actorId={}",
                event.reviewId(), event.receiverId(), event.actorId(), e);
    }

    @Recover
    public void recover(Exception e, CommentEvent event) {
        log.error("NotificationEvent 최종 실패 (Comment): reviewId={}, receiverId={}, actorId={}",
                event.reviewId(), event.receiverId(), event.actorId(), e);
    }

    @Recover
    public void recover(Exception e, ReviewRankedEvent event) {
        log.error("NotificationEvent 최종 실패 (ReviewRanked): reviewId={}, userId={}, rank={}",
                event.reviewId(), event.receiverId(), event.rank(), e);
    }
}