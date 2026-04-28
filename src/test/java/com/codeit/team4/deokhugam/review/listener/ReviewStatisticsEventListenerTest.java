package com.codeit.team4.deokhugam.review.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.codeit.team4.deokhugam.comment.event.CommentCreatedEvent;
import com.codeit.team4.deokhugam.comment.event.CommentDeletedEvent;
import com.codeit.team4.deokhugam.review.entity.ReviewStatistics;
import com.codeit.team4.deokhugam.review.repository.ReviewStatisticsRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReviewStatisticsEventListenerTest {

    @InjectMocks
    private ReviewStatisticsEventListener listener;

    @Mock
    private ReviewStatisticsRepository reviewStatisticsRepository;

    @Test
    @DisplayName("통계 정보가 없을 때 댓글 생성 이벤트 수신 시 통계 생성 및 카운트 증가 성공")
    void handle_comment_created_when_stats_not_exist() {
        // given
        UUID reviewId = UUID.randomUUID();
        CommentCreatedEvent event = new CommentCreatedEvent(reviewId);
        ReviewStatistics newStats = new ReviewStatistics(reviewId);

        given(reviewStatisticsRepository.findById(reviewId)).willReturn(Optional.empty());
        given(reviewStatisticsRepository.save(any(ReviewStatistics.class))).willReturn(newStats);

        // when
        listener.handleCommentCreated(event);

        // then
        verify(reviewStatisticsRepository).save(any(ReviewStatistics.class));
        assertThat(newStats.getCommentCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("통계 정보가 있을 때 댓글 생성 이벤트 수신 시 카운트 증가 성공")
    void handle_comment_created_when_stats_exist() {
        // given
        UUID reviewId = UUID.randomUUID();
        CommentCreatedEvent event = new CommentCreatedEvent(reviewId);
        ReviewStatistics existingStats = new ReviewStatistics(reviewId);
        existingStats.onCommentCreated();

        given(reviewStatisticsRepository.findById(reviewId)).willReturn(Optional.of(existingStats));

        // when
        listener.handleCommentCreated(event);

        // then
        verify(reviewStatisticsRepository, never()).save(any());
        assertThat(existingStats.getCommentCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("통계 정보가 존재할 때 댓글 삭제 이벤트 수신 시 카운트 감소 - 성공")
    void handle_comment_deleted_when_stats_exist() {
        // given
        UUID reviewId = UUID.randomUUID();
        CommentDeletedEvent event = new CommentDeletedEvent(reviewId);
        ReviewStatistics existingStats = new ReviewStatistics(reviewId);
        existingStats.onCommentCreated();

        given(reviewStatisticsRepository.findById(reviewId)).willReturn(Optional.of(existingStats));

        // when
        listener.handleCommentDeleted(event);

        // then
        assertThat(existingStats.getCommentCount()).isZero();
    }

    @Test
    @DisplayName("통계 정보가 없을 때 댓글 삭제 이벤트 수신 시 아무 동작 없이 통과 성공")
    void handle_comment_deleted_when_stats_not_exist() {
        // given
        UUID reviewId = UUID.randomUUID();
        CommentDeletedEvent event = new CommentDeletedEvent(reviewId);

        given(reviewStatisticsRepository.findById(reviewId)).willReturn(Optional.empty());

        // when
        listener.handleCommentDeleted(event);

        // then
        verify(reviewStatisticsRepository).findById(reviewId);
    }

    @Test
    @DisplayName("DB 저장 중 예외 발생 시 댓글 생성 이벤트 처리 실패")
    void handle_comment_created_throws_exception() {
        // given
        UUID reviewId = UUID.randomUUID();
        CommentCreatedEvent event = new CommentCreatedEvent(reviewId);

        given(reviewStatisticsRepository.findById(reviewId)).willReturn(Optional.empty());
        given(reviewStatisticsRepository.save(any(ReviewStatistics.class)))
                .willThrow(new RuntimeException("DB Connection Error"));

        // when & then
        assertThrows(RuntimeException.class, () -> listener.handleCommentCreated(event));
    }
}