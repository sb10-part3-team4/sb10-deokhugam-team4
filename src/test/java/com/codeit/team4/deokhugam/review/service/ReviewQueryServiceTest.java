package com.codeit.team4.deokhugam.review.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeit.team4.deokhugam.book.entity.Book;
import com.codeit.team4.deokhugam.book.repository.BookRepository;
import com.codeit.team4.deokhugam.config.TestContainerConfig;
import com.codeit.team4.deokhugam.global.response.PageResponse;
import com.codeit.team4.deokhugam.global.response.SortDirection;
import com.codeit.team4.deokhugam.review.dto.ReviewOrderBy;
import com.codeit.team4.deokhugam.review.dto.ReviewResponse;
import com.codeit.team4.deokhugam.review.dto.ReviewSearchRequestParam;
import com.codeit.team4.deokhugam.review.entity.Review;
import com.codeit.team4.deokhugam.review.entity.ReviewLike;
import com.codeit.team4.deokhugam.review.repository.ReviewLikeRepository;
import com.codeit.team4.deokhugam.review.repository.ReviewRepository;
import com.codeit.team4.deokhugam.user.entity.User;
import com.codeit.team4.deokhugam.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Import(TestContainerConfig.class)
@ActiveProfiles("test")
@Transactional
class ReviewQueryServiceTest {

    @Autowired
    private ReviewQueryService reviewQueryService;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewLikeRepository reviewLikeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    private User user;
    private User otherUser;
    private Book book;
    private Book otherBook;

    @BeforeEach
    void setUp() {
        user = userRepository.saveAndFlush(new User("test@test.com", "테스터", "password123"));
        otherUser = userRepository.saveAndFlush(new User("other@test.com", "다른사람", "password123"));
        book = bookRepository.saveAndFlush(new Book("클린 코드", "로버트 마틴", "좋은 책", "출판사", LocalDate.of(2024, 1, 1), "1234567890"));
        otherBook = bookRepository.saveAndFlush(new Book("이펙티브 자바", "조슈아 블로크", "좋은 책2", "출판사", LocalDate.of(2024, 2, 1), "1234567891"));
    }

    @Nested
    @DisplayName("기본 목록 조회")
    class BasicSearch {

        @Test
        @DisplayName("리뷰 목록 조회 성공")
        void searchReviews_success() {
            reviewRepository.saveAndFlush(new Review(book, user, "좋은 책입니다", 5));
            reviewRepository.saveAndFlush(new Review(otherBook, user, "괜찮은 책입니다", 3));

            ReviewSearchRequestParam param = new ReviewSearchRequestParam(
                    null, null, null, ReviewOrderBy.CREATED_AT, SortDirection.DESC,
                    null, null, 50, user.getId()
            );

            PageResponse<ReviewResponse> result = reviewQueryService.searchReviews(param);

            assertThat(result.content()).hasSize(2);
            assertThat(result.totalElements()).isEqualTo(2);
            assertThat(result.hasNext()).isFalse();
        }
    }

    @Nested
    @DisplayName("페이지네이션")
    class Pagination {

        @Test
        @DisplayName("다음 페이지 존재 확인 성공")
        void searchReviews_hasNext_success() {
            reviewRepository.saveAndFlush(new Review(book, user, "리뷰1", 5));
            reviewRepository.saveAndFlush(new Review(otherBook, user, "리뷰2", 4));
            reviewRepository.saveAndFlush(new Review(
                    bookRepository.saveAndFlush(new Book("책3", "저자", "설명", "출판사", LocalDate.of(2024, 3, 1), "1234567892")),
                    user, "리뷰3", 3));

            ReviewSearchRequestParam param = new ReviewSearchRequestParam(
                    null, null, null, ReviewOrderBy.CREATED_AT, SortDirection.DESC,
                    null, null, 2, user.getId()
            );

            PageResponse<ReviewResponse> result = reviewQueryService.searchReviews(param);

            assertThat(result.content()).hasSize(2);
            assertThat(result.totalElements()).isEqualTo(3);
            assertThat(result.hasNext()).isTrue();
            assertThat(result.nextCursor()).isNotNull();
            assertThat(result.nextAfter()).isNotNull();
        }

        @Test
        @DisplayName("커서로 다음 페이지 조회 성공")
        void searchReviews_cursorPagination_success() {
            reviewRepository.saveAndFlush(new Review(book, user, "리뷰1", 5));
            reviewRepository.saveAndFlush(new Review(otherBook, user, "리뷰2", 4));
            reviewRepository.saveAndFlush(new Review(
                    bookRepository.saveAndFlush(new Book("책3", "저자", "설명", "출판사", LocalDate.of(2024, 3, 1), "1234567892")),
                    user, "리뷰3", 3));

            // 1페이지
            ReviewSearchRequestParam firstPage = new ReviewSearchRequestParam(
                    null, null, null, ReviewOrderBy.CREATED_AT, SortDirection.DESC,
                    null, null, 2, user.getId()
            );
            PageResponse<ReviewResponse> firstResult = reviewQueryService.searchReviews(firstPage);

            // 2페이지 (커서 사용)
            ReviewSearchRequestParam secondPage = new ReviewSearchRequestParam(
                    null, null, null, ReviewOrderBy.CREATED_AT, SortDirection.DESC,
                    firstResult.nextCursor(), firstResult.nextAfter(), 2, user.getId()
            );
            PageResponse<ReviewResponse> secondResult = reviewQueryService.searchReviews(secondPage);

            assertThat(secondResult.content()).hasSize(1);
            assertThat(secondResult.totalElements()).isEqualTo(3);
            assertThat(secondResult.hasNext()).isFalse();
            // 1페이지와 2페이지 리뷰가 중복 없이 전체를 커버하는지 검증
            List<UUID> allIds = new ArrayList<>(firstResult.content().stream().map(ReviewResponse::id).toList());
            allIds.addAll(secondResult.content().stream().map(ReviewResponse::id).toList());
            assertThat(allIds).hasSize(3).doesNotHaveDuplicates();
        }

        @Test
        @DisplayName("rating 정렬 커서 페이지네이션 성공")
        void searchReviews_ratingCursorPagination_success() {
            reviewRepository.saveAndFlush(new Review(book, user, "리뷰1", 5));
            reviewRepository.saveAndFlush(new Review(otherBook, user, "리뷰2", 3));
            reviewRepository.saveAndFlush(new Review(
                    bookRepository.saveAndFlush(new Book("책3", "저자", "설명", "출판사", LocalDate.of(2024, 3, 1), "1234567892")),
                    user, "리뷰3", 1));

            // 1페이지 (rating DESC)
            ReviewSearchRequestParam firstPage = new ReviewSearchRequestParam(
                    null, null, null, ReviewOrderBy.RATING, SortDirection.DESC,
                    null, null, 2, user.getId()
            );
            PageResponse<ReviewResponse> firstResult = reviewQueryService.searchReviews(firstPage);

            assertThat(firstResult.content()).hasSize(2);
            assertThat(firstResult.content().get(0).rating()).isEqualTo(5);
            assertThat(firstResult.content().get(1).rating()).isEqualTo(3);

            // 2페이지 (커서 사용)
            ReviewSearchRequestParam secondPage = new ReviewSearchRequestParam(
                    null, null, null, ReviewOrderBy.RATING, SortDirection.DESC,
                    firstResult.nextCursor(), firstResult.nextAfter(), 2, user.getId()
            );
            PageResponse<ReviewResponse> secondResult = reviewQueryService.searchReviews(secondPage);

            assertThat(secondResult.content()).hasSize(1);
            assertThat(secondResult.totalElements()).isEqualTo(3);
            assertThat(secondResult.content().get(0).rating()).isEqualTo(1);
            assertThat(secondResult.hasNext()).isFalse();
        }
    }

    @Nested
    @DisplayName("좋아요 여부")
    class LikedByMe {

        @Test
        @DisplayName("좋아요 한 리뷰 likedByMe 조회 성공")
        void searchReviews_likedByMe_true_success() {
            Review review = reviewRepository.saveAndFlush(new Review(book, user, "좋은 책입니다", 5));
            reviewLikeRepository.saveAndFlush(new ReviewLike(review, otherUser));

            ReviewSearchRequestParam param = new ReviewSearchRequestParam(
                    null, null, null, ReviewOrderBy.CREATED_AT, SortDirection.DESC,
                    null, null, 50, otherUser.getId()
            );

            PageResponse<ReviewResponse> result = reviewQueryService.searchReviews(param);

            assertThat(result.content()).hasSize(1);
            assertThat(result.totalElements()).isEqualTo(1);
            assertThat(result.content().get(0).likedByMe()).isTrue();
        }

        @Test
        @DisplayName("좋아요 안 한 리뷰 likedByMe 조회 성공")
        void searchReviews_likedByMe_false_success() {
            reviewRepository.saveAndFlush(new Review(book, user, "좋은 책입니다", 5));

            ReviewSearchRequestParam param = new ReviewSearchRequestParam(
                    null, null, null, ReviewOrderBy.CREATED_AT, SortDirection.DESC,
                    null, null, 50, otherUser.getId()
            );

            PageResponse<ReviewResponse> result = reviewQueryService.searchReviews(param);

            assertThat(result.content()).hasSize(1);
            assertThat(result.totalElements()).isEqualTo(1);
            assertThat(result.content().get(0).likedByMe()).isFalse();
        }
    }
}
