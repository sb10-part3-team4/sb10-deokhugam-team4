package com.codeit.team4.deokhugam.review.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReviewStatisticsTest {

    @Test
    @DisplayName("최초 생성 시 댓글 수는 0으로 초기화 성공")
    void create_review_statistics() {
        // given
        UUID reviewId = UUID.randomUUID();

        // when
        ReviewStatistics stats = new ReviewStatistics(reviewId);

        // then
        assertThat(stats.getReviewId()).isEqualTo(reviewId);
        assertThat(stats.getCommentCount()).isZero();
    }

    @Test
    @DisplayName("onCommentCreated 호출 시 댓글 수 1 증가 성공")
    void on_comment_created_increases_count() {
        // given
        ReviewStatistics stats = new ReviewStatistics(UUID.randomUUID());

        // when
        stats.onCommentCreated();

        // then
        assertThat(stats.getCommentCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("onCommentDeleted 호출 시 댓글 수 1 감소 성공")
    void on_comment_deleted_decreases_count() {
        // given
        ReviewStatistics stats = new ReviewStatistics(UUID.randomUUID());
        stats.onCommentCreated();
        stats.onCommentCreated();

        // when
        stats.onCommentDeleted();

        // then
        assertThat(stats.getCommentCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("댓글 수가 0일 때 onCommentDeleted 호출 시 음수 방지 성공")
    void on_comment_deleted_prevents_negative_count() {
        // given
        ReviewStatistics stats = new ReviewStatistics(UUID.randomUUID());
        // 초기 상태 count 0

        // when
        stats.onCommentDeleted();

        // then
        assertThat(stats.getCommentCount()).isZero();
    }
}