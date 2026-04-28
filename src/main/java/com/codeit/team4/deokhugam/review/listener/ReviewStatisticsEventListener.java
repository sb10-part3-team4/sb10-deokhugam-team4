package com.codeit.team4.deokhugam.review.listener;

import com.codeit.team4.deokhugam.comment.event.CommentCreatedEvent;
import com.codeit.team4.deokhugam.comment.event.CommentDeletedEvent;
import com.codeit.team4.deokhugam.global.lock.DistributedLock;
import com.codeit.team4.deokhugam.review.entity.ReviewStatistics;
import com.codeit.team4.deokhugam.review.repository.ReviewStatisticsRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewStatisticsEventListener {

    private final ReviewStatisticsRepository reviewStatisticsRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @DistributedLock(key = "deokhugam:review-statistics", lockParam = {"event.reviewId"})
    @Retryable(retryFor = PessimisticLockingFailureException.class, maxAttempts = 3)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleCommentCreated(CommentCreatedEvent event) {
        ReviewStatistics stats = findOrCreateStats(event.reviewId());
        stats.onCommentCreated();
        log.info("ReviewStatistics 갱신 완료 (CommentCreated): reviewId={}, count={}",
                event.reviewId(), stats.getCommentCount());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @DistributedLock(key = "deokhugam:review-statistics", lockParam = {"event.reviewId"})
    @Retryable(retryFor = PessimisticLockingFailureException.class, maxAttempts = 3)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleCommentDeleted(CommentDeletedEvent event) {
        reviewStatisticsRepository.findById(event.reviewId()).ifPresent(stats -> {
            stats.onCommentDeleted();
            log.info("ReviewStatistics 갱신 완료 (CommentDeleted): reviewId={}, count={}",
                    event.reviewId(), stats.getCommentCount());
        });
    }

    private ReviewStatistics findOrCreateStats(UUID reviewId) {
        return reviewStatisticsRepository.findById(reviewId)
                .orElseGet(() -> reviewStatisticsRepository.save(new ReviewStatistics(reviewId)));
    }
}
