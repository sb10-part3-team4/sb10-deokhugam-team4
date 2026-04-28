package com.codeit.team4.deokhugam.book.listener;

import com.codeit.team4.deokhugam.book.entity.BookStatistics;
import com.codeit.team4.deokhugam.book.repository.BookStatisticsRepository;
import com.codeit.team4.deokhugam.global.lock.DistributedLock;
import com.codeit.team4.deokhugam.review.event.ReviewCreatedEvent;
import com.codeit.team4.deokhugam.review.event.ReviewDeletedEvent;
import com.codeit.team4.deokhugam.review.event.ReviewUpdatedEvent;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.PessimisticLockingFailureException;
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
    @Retryable(
            retryFor = PessimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 500)
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleReviewCreated(ReviewCreatedEvent event) {
        log.debug("ReviewCreatedEvent 수신: {}", event);

        BookStatistics statistics = bookStatisticsRepository.findById(event.bookId())
                .orElseGet(() -> new BookStatistics(event.bookId()));
        statistics.onReviewCreated(event.rating());
        bookStatisticsRepository.save(statistics);

        log.info("BookStatistics 갱신 완료 (ReviewCreated): bookId={}, count={}, sum={}",
                event.bookId(), statistics.getReviewCount(), statistics.getRatingSum());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @DistributedLock(key = "deokhugam:book-statistics", lockParam = {"event.bookId"})
    @Retryable(
            retryFor = PessimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 500)
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleReviewUpdated(ReviewUpdatedEvent event) {
        log.debug("ReviewUpdatedEvent 수신: {}", event);

        findStatistics(event.bookId(), "업데이트").ifPresent(statistics -> {
            statistics.onReviewUpdated(event.oldRating(), event.newRating());
            log.info("BookStatistics 갱신 완료 (ReviewUpdated): bookId={}, count={}, sum={}",
                    event.bookId(), statistics.getReviewCount(), statistics.getRatingSum());
        });
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @DistributedLock(key = "deokhugam:book-statistics", lockParam = {"event.bookId"})
    @Retryable(
            retryFor = PessimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 500)
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleReviewDeleted(ReviewDeletedEvent event) {
        log.debug("ReviewDeletedEvent 수신: {}", event);

        findStatistics(event.bookId(), "삭제").ifPresent(statistics -> {
            statistics.onReviewDeleted(event.rating());
            log.info("BookStatistics 갱신 완료 (ReviewDeleted): bookId={}, count={}, sum={}",
                    event.bookId(), statistics.getReviewCount(), statistics.getRatingSum());
        });
    }

    private Optional<BookStatistics> findStatistics(UUID bookId, String eventType) {
        Optional<BookStatistics> statistics = bookStatisticsRepository.findById(bookId);
        if (statistics.isEmpty()) {
            log.warn("BookStatistics 미존재로 {} 누락: bookId={}", eventType, bookId);
        }
        return statistics;
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
