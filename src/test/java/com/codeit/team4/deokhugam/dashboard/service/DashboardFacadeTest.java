package com.codeit.team4.deokhugam.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.codeit.team4.deokhugam.book.entity.Book;
import com.codeit.team4.deokhugam.global.error.BusinessException;
import com.codeit.team4.deokhugam.book.repository.BookRepository;
import com.codeit.team4.deokhugam.config.TestContainerConfig;
import com.codeit.team4.deokhugam.dashboard.dto.PopularBookResponse;
import com.codeit.team4.deokhugam.dashboard.dto.PopularReviewResponse;
import com.codeit.team4.deokhugam.dashboard.dto.DashboardSearchRequestParam;
import com.codeit.team4.deokhugam.dashboard.entity.PeriodType;
import com.codeit.team4.deokhugam.global.response.PageResponse;
import com.codeit.team4.deokhugam.global.response.SortDirection;
import com.codeit.team4.deokhugam.review.entity.Review;
import com.codeit.team4.deokhugam.review.repository.ReviewRepository;
import com.codeit.team4.deokhugam.user.entity.User;
import com.codeit.team4.deokhugam.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Import(TestContainerConfig.class)
@ActiveProfiles("test")
@Transactional
class DashboardFacadeTest {

    @Autowired
    private DashboardFacade dashboardService;

    @Autowired
    private DashboardBatchService dashboardBatchService;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Value("${dashboard.batch.zone}")
    private String zone;

    private User user;

    @BeforeEach
    void setUp() {
        user = userRepository.saveAndFlush(new User("test@test.com", "테스터", "password123"));
    }

    private Book createBook(String title, String isbn) {
        return bookRepository.saveAndFlush(
                new Book(title, "저자", "설명", "출판사", LocalDate.of(2024, 1, 1), isbn)
        );
    }

    private void runBatch(LocalDate snapshotDate) {
        dashboardBatchService.updatePopularBooks(snapshotDate);
        dashboardBatchService.updatePopularReviews(snapshotDate);
    }

    @Nested
    @DisplayName("인기 도서 조회")
    class GetPopularBooks {

        @Test
        @DisplayName("인기 도서 조회 성공")
        void getPopularBooks_success() {
            LocalDate snapshotDate = LocalDate.now(ZoneId.of(zone));
            Book book1 = createBook("책1", "1111111111");
            Book book2 = createBook("책2", "2222222222");
            reviewRepository.saveAndFlush(new Review(book1, user, "좋아요", 5));
            reviewRepository.saveAndFlush(new Review(book2, user, "괜찮아요", 3));
            runBatch(snapshotDate);

            DashboardSearchRequestParam param = new DashboardSearchRequestParam(
                    PeriodType.DAILY, SortDirection.ASC, null, null, 50
            );

            PageResponse<PopularBookResponse> result = dashboardService.getPopularBooks(param);

            assertThat(result.content()).hasSize(2);
            assertThat(result.content().get(0).rank()).isEqualTo(1);
            assertThat(result.content().get(1).rank()).isEqualTo(2);
            assertThat(result.hasNext()).isFalse();
            assertThat(result.totalElements()).isNull();
        }

        @Test
        @DisplayName("DESC 정렬 조회 성공")
        void getPopularBooks_desc_success() {
            LocalDate snapshotDate = LocalDate.now(ZoneId.of(zone));
            Book book1 = createBook("책1", "1111111111");
            Book book2 = createBook("책2", "2222222222");
            reviewRepository.saveAndFlush(new Review(book1, user, "좋아요", 5));
            reviewRepository.saveAndFlush(new Review(book2, user, "괜찮아요", 3));
            runBatch(snapshotDate);

            DashboardSearchRequestParam param = new DashboardSearchRequestParam(
                    PeriodType.DAILY, SortDirection.DESC, null, null, 50
            );

            PageResponse<PopularBookResponse> result = dashboardService.getPopularBooks(param);

            assertThat(result.content()).hasSize(2);
            assertThat(result.content().get(0).rank()).isEqualTo(2);
            assertThat(result.content().get(1).rank()).isEqualTo(1);
        }

        @Test
        @DisplayName("커서 페이지네이션 성공")
        void getPopularBooks_cursor_success() {
            LocalDate snapshotDate = LocalDate.now(ZoneId.of(zone));
            for (int i = 0; i < 3; i++) {
                Book book = createBook("책" + i, "100000000" + i);
                reviewRepository.saveAndFlush(new Review(book, user, "리뷰" + i, 5 - i));
            }
            runBatch(snapshotDate);

            // 1페이지
            DashboardSearchRequestParam firstPage = new DashboardSearchRequestParam(
                    PeriodType.DAILY, SortDirection.ASC, null, null, 2
            );
            PageResponse<PopularBookResponse> firstResult = dashboardService.getPopularBooks(firstPage);

            assertThat(firstResult.content()).hasSize(2);
            assertThat(firstResult.hasNext()).isTrue();
            assertThat(firstResult.nextCursor()).isNotNull();

            // 2페이지
            DashboardSearchRequestParam secondPage = new DashboardSearchRequestParam(
                    PeriodType.DAILY, SortDirection.ASC,
                    firstResult.nextCursor(), firstResult.nextAfter(), 2
            );
            PageResponse<PopularBookResponse> secondResult = dashboardService.getPopularBooks(secondPage);

            assertThat(secondResult.content()).hasSize(1);
            assertThat(secondResult.hasNext()).isFalse();
        }

        @Test
        @DisplayName("데이터 없을 때 빈 결과 반환 성공")
        void getPopularBooks_empty_success() {
            runBatch(LocalDate.now(ZoneId.of(zone)));

            DashboardSearchRequestParam param = new DashboardSearchRequestParam(
                    PeriodType.DAILY, SortDirection.ASC, null, null, 50
            );

            PageResponse<PopularBookResponse> result = dashboardService.getPopularBooks(param);

            assertThat(result.content()).isEmpty();
            assertThat(result.hasNext()).isFalse();
        }

        @Test
        @DisplayName("잘못된 cursor로 조회 실패")
        void getPopularBooks_invalidCursor_fail() {
            LocalDate snapshotDate = LocalDate.now(ZoneId.of(zone));
            Book book = createBook("책1", "1111111111");
            reviewRepository.saveAndFlush(new Review(book, user, "좋아요", 5));
            runBatch(snapshotDate);

            DashboardSearchRequestParam param = new DashboardSearchRequestParam(
                    PeriodType.DAILY, SortDirection.ASC, "invalid", null, 50
            );

            assertThatThrownBy(() -> dashboardService.getPopularBooks(param))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("인기 리뷰 조회")
    class GetPopularReviews {

        @Test
        @DisplayName("인기 리뷰 조회 성공")
        void getPopularReviews_success() {
            LocalDate snapshotDate = LocalDate.now(ZoneId.of(zone));
            Book book1 = createBook("책1", "1111111111");
            Book book2 = createBook("책2", "2222222222");
            reviewRepository.saveAndFlush(new Review(book1, user, "좋아요", 5));
            reviewRepository.saveAndFlush(new Review(book2, user, "괜찮아요", 3));
            runBatch(snapshotDate);

            DashboardSearchRequestParam param = new DashboardSearchRequestParam(
                    PeriodType.DAILY, SortDirection.ASC, null, null, 50
            );

            PageResponse<PopularReviewResponse> result = dashboardService.getPopularReviews(param);

            assertThat(result.content()).hasSize(2);
            assertThat(result.content().get(0).rank()).isEqualTo(1);
            assertThat(result.content().get(1).rank()).isEqualTo(2);
            assertThat(result.hasNext()).isFalse();
            assertThat(result.totalElements()).isNull();
        }

        @Test
        @DisplayName("데이터 없을 때 빈 결과 반환 성공")
        void getPopularReviews_empty_success() {
            runBatch(LocalDate.now(ZoneId.of(zone)));

            DashboardSearchRequestParam param = new DashboardSearchRequestParam(
                    PeriodType.DAILY, SortDirection.ASC, null, null, 50
            );

            PageResponse<PopularReviewResponse> result = dashboardService.getPopularReviews(param);

            assertThat(result.content()).isEmpty();
            assertThat(result.hasNext()).isFalse();
        }

        @Test
        @DisplayName("DESC 정렬 조회 성공")
        void getPopularReviews_desc_success() {
            LocalDate snapshotDate = LocalDate.now(ZoneId.of(zone));
            Book book1 = createBook("책1", "1111111111");
            Book book2 = createBook("책2", "2222222222");
            reviewRepository.saveAndFlush(new Review(book1, user, "좋아요", 5));
            reviewRepository.saveAndFlush(new Review(book2, user, "괜찮아요", 3));
            runBatch(snapshotDate);

            DashboardSearchRequestParam param = new DashboardSearchRequestParam(
                    PeriodType.DAILY, SortDirection.DESC, null, null, 50
            );

            PageResponse<PopularReviewResponse> result = dashboardService.getPopularReviews(param);

            assertThat(result.content()).hasSize(2);
            assertThat(result.content().get(0).rank()).isEqualTo(2);
            assertThat(result.content().get(1).rank()).isEqualTo(1);
        }

        @Test
        @DisplayName("커서 페이지네이션 성공")
        void getPopularReviews_cursor_success() {
            LocalDate snapshotDate = LocalDate.now(ZoneId.of(zone));
            for (int i = 0; i < 3; i++) {
                Book book = createBook("책" + i, "100000000" + i);
                reviewRepository.saveAndFlush(new Review(book, user, "리뷰" + i, 5 - i));
            }
            runBatch(snapshotDate);

            DashboardSearchRequestParam firstPage = new DashboardSearchRequestParam(
                    PeriodType.DAILY, SortDirection.ASC, null, null, 2
            );
            PageResponse<PopularReviewResponse> firstResult = dashboardService.getPopularReviews(firstPage);

            assertThat(firstResult.content()).hasSize(2);
            assertThat(firstResult.hasNext()).isTrue();
            assertThat(firstResult.nextCursor()).isNotNull();

            DashboardSearchRequestParam secondPage = new DashboardSearchRequestParam(
                    PeriodType.DAILY, SortDirection.ASC,
                    firstResult.nextCursor(), firstResult.nextAfter(), 2
            );
            PageResponse<PopularReviewResponse> secondResult = dashboardService.getPopularReviews(secondPage);

            assertThat(secondResult.content()).hasSize(1);
            assertThat(secondResult.hasNext()).isFalse();
        }

        @Test
        @DisplayName("잘못된 cursor로 조회 실패")
        void getPopularReviews_invalidCursor_fail() {
            LocalDate snapshotDate = LocalDate.now(ZoneId.of(zone));
            Book book = createBook("책1", "1111111111");
            reviewRepository.saveAndFlush(new Review(book, user, "좋아요", 5));
            runBatch(snapshotDate);

            DashboardSearchRequestParam param = new DashboardSearchRequestParam(
                    PeriodType.DAILY, SortDirection.ASC, "invalid", null, 50
            );

            assertThatThrownBy(() -> dashboardService.getPopularReviews(param))
                    .isInstanceOf(BusinessException.class);
        }
    }
}
