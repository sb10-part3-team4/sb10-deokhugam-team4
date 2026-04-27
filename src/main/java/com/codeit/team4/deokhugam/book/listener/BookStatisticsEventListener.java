package com.codeit.team4.deokhugam.book.listener;

import com.codeit.team4.deokhugam.book.entity.BookStatistics;
import com.codeit.team4.deokhugam.book.repository.BookStatisticsRepository;
import com.codeit.team4.deokhugam.global.lock.DistributedLock;
import com.codeit.team4.deokhugam.review.event.ReviewCreatedEvent;
import com.codeit.team4.deokhugam.review.event.ReviewDeletedEvent;
import com.codeit.team4.deokhugam.review.event.ReviewUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookStatisticsEventListener {

    private final BookStatisticsRepository bookStatisticsRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @DistributedLock(key = "deokhugam:book-statistics", lockParam = {"event.bookId"})
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 500))
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleReviewCreated(ReviewCreatedEvent event) {
        log.debug("ReviewCreatedEvent 수신: {}", event);

        BookStatistics statistics = bookStatisticsRepository.findById(event.bookId())
                .orElseGet(() -> bookStatisticsRepository.save(
                        new BookStatistics(event.bookId())));

        statistics.onReviewCreated(event.rating());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @DistributedLock(key = "deokhugam:book-statistics", lockParam = {"event.bookId"})
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 500))
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleReviewUpdated(ReviewUpdatedEvent event) {
        log.debug("ReviewUpdatedEvent 수신: {}", event);

        bookStatisticsRepository.findById(event.bookId())
                .ifPresent(statistics ->
                        statistics.onReviewUpdated(event.oldRating(), event.newRating()));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @DistributedLock(key = "deokhugam:book-statistics", lockParam = {"event.bookId"})
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 500))
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleReviewDeleted(ReviewDeletedEvent event) {
        log.debug("ReviewDeletedEvent 수신: {}", event);

        bookStatisticsRepository.findById(event.bookId())
                .ifPresent(statistics -> statistics.onReviewDeleted(event.rating()));
    }

    @Recover
    public void recover(Exception e, ReviewCreatedEvent event) {
        log.error("BookStatistics 갱신 최종 실패 (ReviewCreated): bookId={}, rating={}",
                event.bookId(), event.rating(), e);
    }

    @Recover
    public void recover(Exception e, ReviewUpdatedEvent event) {
        log.error("BookStatistics 갱신 최종 실패 (ReviewUpdated): bookId={}, oldRating={}, newRating={}",
                event.bookId(), event.oldRating(), event.newRating(), e);
    }

    @Recover
    public void recover(Exception e, ReviewDeletedEvent event) {
        log.error("BookStatistics 갱신 최종 실패 (ReviewDeleted): bookId={}, rating={}",
                event.bookId(), event.rating(), e);
    }
}
