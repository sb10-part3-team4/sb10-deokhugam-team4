package com.codeit.team4.deokhugam.book.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.codeit.team4.deokhugam.book.entity.BookStatistics;
import com.codeit.team4.deokhugam.book.repository.BookStatisticsRepository;
import com.codeit.team4.deokhugam.review.event.ReviewCreatedEvent;
import com.codeit.team4.deokhugam.review.event.ReviewDeletedEvent;
import com.codeit.team4.deokhugam.review.event.ReviewUpdatedEvent;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookStatisticsEventListenerTest {

    @InjectMocks
    private BookStatisticsEventListener listener;

    @Mock
    private BookStatisticsRepository bookStatisticsRepository;

    @Nested
    @DisplayName("handleReviewCreated")
    class HandleReviewCreated {

        @Test
        @DisplayName("기존 통계가 있을 때 리뷰 생성 이벤트 처리 성공")
        void handleReviewCreated_existingStatistics_success() {
            UUID bookId = UUID.randomUUID();
            BookStatistics statistics = new BookStatistics(bookId);
            ReviewCreatedEvent event = new ReviewCreatedEvent(bookId, 5);

            given(bookStatisticsRepository.findById(bookId))
                    .willReturn(Optional.of(statistics));

            listener.handleReviewCreated(event);

            verify(bookStatisticsRepository).findById(bookId);
            verify(bookStatisticsRepository).save(statistics);
            assertThat(statistics.getReviewCount()).isEqualTo(1);
            assertThat(statistics.getRatingSum()).isEqualTo(5);
        }

        @Test
        @DisplayName("통계가 없을 때 새로 생성 후 리뷰 생성 이벤트 처리 성공")
        void handleReviewCreated_newStatistics_success() {
            UUID bookId = UUID.randomUUID();
            BookStatistics statistics = new BookStatistics(bookId);
            ReviewCreatedEvent event = new ReviewCreatedEvent(bookId, 4);

            given(bookStatisticsRepository.findById(bookId))
                    .willReturn(Optional.empty());
            given(bookStatisticsRepository.save(any(BookStatistics.class)))
                    .willReturn(statistics);

            listener.handleReviewCreated(event);

            verify(bookStatisticsRepository).save(any(BookStatistics.class));
        }
    }

    @Nested
    @DisplayName("handleReviewUpdated")
    class HandleReviewUpdated {

        @Test
        @DisplayName("리뷰 수정 이벤트 처리 성공")
        void handleReviewUpdated_success() {
            UUID bookId = UUID.randomUUID();
            BookStatistics statistics = new BookStatistics(bookId);
            statistics.onReviewCreated(3);
            ReviewUpdatedEvent event = new ReviewUpdatedEvent(bookId, 3, 5);

            given(bookStatisticsRepository.findById(bookId))
                    .willReturn(Optional.of(statistics));

            listener.handleReviewUpdated(event);

            verify(bookStatisticsRepository).findById(bookId);
            assertThat(statistics.getReviewCount()).isEqualTo(1);
            assertThat(statistics.getRatingSum()).isEqualTo(5);
        }

        @Test
        @DisplayName("통계가 없을 때 리뷰 수정 이벤트 무시 성공")
        void handleReviewUpdated_noStatistics_success() {
            UUID bookId = UUID.randomUUID();
            ReviewUpdatedEvent event = new ReviewUpdatedEvent(bookId, 3, 5);

            given(bookStatisticsRepository.findById(bookId))
                    .willReturn(Optional.empty());

            listener.handleReviewUpdated(event);

            verify(bookStatisticsRepository).findById(bookId);
        }
    }

    @Nested
    @DisplayName("handleReviewDeleted")
    class HandleReviewDeleted {

        @Test
        @DisplayName("리뷰 삭제 이벤트 처리 성공")
        void handleReviewDeleted_success() {
            UUID bookId = UUID.randomUUID();
            BookStatistics statistics = new BookStatistics(bookId);
            statistics.onReviewCreated(4);
            ReviewDeletedEvent event = new ReviewDeletedEvent(bookId, 4);

            given(bookStatisticsRepository.findById(bookId))
                    .willReturn(Optional.of(statistics));

            listener.handleReviewDeleted(event);

            verify(bookStatisticsRepository).findById(bookId);
            assertThat(statistics.getReviewCount()).isEqualTo(0);
            assertThat(statistics.getRatingSum()).isEqualTo(0);
        }

        @Test
        @DisplayName("통계가 없을 때 리뷰 삭제 이벤트 무시 성공")
        void handleReviewDeleted_noStatistics_success() {
            UUID bookId = UUID.randomUUID();
            ReviewDeletedEvent event = new ReviewDeletedEvent(bookId, 4);

            given(bookStatisticsRepository.findById(bookId))
                    .willReturn(Optional.empty());

            listener.handleReviewDeleted(event);

            verify(bookStatisticsRepository).findById(bookId);
        }
    }

    @Nested
    @DisplayName("handleReviewCreated 예외 발생")
    class HandleReviewCreatedException {

        @Test
        @DisplayName("repository 예외 시 예외 전파 성공")
        void handleReviewCreated_exception_success() {
            UUID bookId = UUID.randomUUID();
            ReviewCreatedEvent event = new ReviewCreatedEvent(bookId, 5);

            given(bookStatisticsRepository.findById(bookId))
                    .willThrow(new RuntimeException("DB 연결 실패"));

            assertThatCode(() -> listener.handleReviewCreated(event))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("recover")
    class Recover {

        @Test
        @DisplayName("ReviewCreatedEvent recover 시 예외 전파 없이 종료 성공")
        void recover_reviewCreated_success() {
            UUID bookId = UUID.randomUUID();
            ReviewCreatedEvent event = new ReviewCreatedEvent(bookId, 5);

            assertThatCode(() -> listener.recover(new RuntimeException("실패"), event))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("ReviewUpdatedEvent recover 시 예외 전파 없이 종료 성공")
        void recover_reviewUpdated_success() {
            UUID bookId = UUID.randomUUID();
            ReviewUpdatedEvent event = new ReviewUpdatedEvent(bookId, 3, 5);

            assertThatCode(() -> listener.recover(new RuntimeException("실패"), event))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("ReviewDeletedEvent recover 시 예외 전파 없이 종료 성공")
        void recover_reviewDeleted_success() {
            UUID bookId = UUID.randomUUID();
            ReviewDeletedEvent event = new ReviewDeletedEvent(bookId, 4);

            assertThatCode(() -> listener.recover(new RuntimeException("실패"), event))
                    .doesNotThrowAnyException();
        }
    }
}
