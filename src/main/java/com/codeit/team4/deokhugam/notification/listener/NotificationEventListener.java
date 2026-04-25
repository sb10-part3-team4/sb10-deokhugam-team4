package com.codeit.team4.deokhugam.notification.listener;

import com.codeit.team4.deokhugam.notification.service.NotificationService;
import com.codeit.team4.deokhugam.notification.event.CommentEvent;
import com.codeit.team4.deokhugam.notification.event.LikeEvent;
import com.codeit.team4.deokhugam.notification.event.ReviewRankedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleLikeCreated(LikeEvent event) {
        log.debug("LikeEvent 수신: {}", event);

        if (event.receiverId().equals(event.actorId())) {
            return;
        }

        notificationService.createLikeNotification(
                event.receiverId(),
                event.reviewId(),
                event.actorId()
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleCommentCreated(CommentEvent event) {
        log.debug("CommentEvent 수신: {}", event);

        if (event.receiverId().equals(event.actorId())) {
            return;
        }

        notificationService.createCommentNotification(
                event.receiverId(),
                event.reviewId(),
                event.actorId()
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
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
}