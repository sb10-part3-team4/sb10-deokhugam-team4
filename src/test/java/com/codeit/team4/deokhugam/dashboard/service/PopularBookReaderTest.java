package com.codeit.team4.deokhugam.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeit.team4.deokhugam.book.entity.Book;
import com.codeit.team4.deokhugam.book.repository.BookRepository;
import com.codeit.team4.deokhugam.config.TestContainerConfig;
import com.codeit.team4.deokhugam.dashboard.dto.PopularBookSearchRequestParam;
import com.codeit.team4.deokhugam.dashboard.entity.PeriodType;
import com.codeit.team4.deokhugam.dashboard.model.PopularBookViewModel;
import com.codeit.team4.deokhugam.global.response.SortDirection;
import com.codeit.team4.deokhugam.review.entity.Review;
import com.codeit.team4.deokhugam.review.repository.ReviewRepository;
import com.codeit.team4.deokhugam.user.entity.User;
import com.codeit.team4.deokhugam.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
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
class PopularBookReaderTest {

    @Autowired
    private PopularBookReader popularBookReader;

    @Autowired
    private DashboardBatchService dashboardBatchService;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

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

    private void runBatch() {
        dashboardBatchService.updatePopularBooks(LocalDate.of(2026, 4, 23));
    }

    @Nested
    @DisplayName("최신 스냅샷 날짜 조회")
    class FindLatestSnapshotDate {

        @Test
        @DisplayName("최신 스냅샷 날짜 조회 성공")
        void findLatestSnapshotDate_success() {
            Book book = createBook("책1", "1111111111");
            reviewRepository.saveAndFlush(new Review(book, user, "좋아요", 5));
            runBatch();

            LocalDate result = popularBookReader.findLatestSnapshotDate(PeriodType.DAILY);

            assertThat(result).isEqualTo(LocalDate.of(2026, 4, 23));
        }

        @Test
        @DisplayName("데이터 없을 때 null 반환 성공")
        void findLatestSnapshotDate_empty_success() {
            LocalDate result = popularBookReader.findLatestSnapshotDate(PeriodType.DAILY);

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("인기 도서 조회")
    class FindPopularBooks {

        @Test
        @DisplayName("ASC 정렬 조회 성공")
        void findPopularBooks_asc_success() {
            Book book1 = createBook("책1", "1111111111");
            Book book2 = createBook("책2", "2222222222");
            reviewRepository.saveAndFlush(new Review(book1, user, "좋아요", 5));
            reviewRepository.saveAndFlush(new Review(book2, user, "괜찮아요", 3));
            runBatch();

            LocalDate snapshotDate = popularBookReader.findLatestSnapshotDate(PeriodType.DAILY);
            PopularBookSearchRequestParam param = new PopularBookSearchRequestParam(
                    PeriodType.DAILY, SortDirection.ASC, null, null, 50
            );

            List<PopularBookViewModel> results = popularBookReader.findPopularBooks(param, snapshotDate);

            assertThat(results).hasSize(2);
            assertThat(results.get(0).rank()).isLessThan(results.get(1).rank());
        }

        @Test
        @DisplayName("커서로 다음 페이지 조회 성공")
        void findPopularBooks_cursor_success() {
            for (int i = 0; i < 3; i++) {
                Book book = createBook("책" + i, "100000000" + i);
                reviewRepository.saveAndFlush(new Review(book, user, "리뷰" + i, 5 - i));
            }
            runBatch();

            LocalDate snapshotDate = popularBookReader.findLatestSnapshotDate(PeriodType.DAILY);

            // 1페이지
            PopularBookSearchRequestParam firstPage = new PopularBookSearchRequestParam(
                    PeriodType.DAILY, SortDirection.ASC, null, null, 2
            );
            List<PopularBookViewModel> firstResults = popularBookReader.findPopularBooks(firstPage, snapshotDate);

            assertThat(firstResults).hasSize(3); // limit+1

            // 2페이지
            PopularBookSearchRequestParam secondPage = new PopularBookSearchRequestParam(
                    PeriodType.DAILY, SortDirection.ASC,
                    String.valueOf(firstResults.get(1).rank()), null, 2
            );
            List<PopularBookViewModel> secondResults = popularBookReader.findPopularBooks(secondPage, snapshotDate);

            assertThat(secondResults).hasSize(1);
        }
    }
}
