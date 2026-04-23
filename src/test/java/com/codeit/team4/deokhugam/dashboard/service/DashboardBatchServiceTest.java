package com.codeit.team4.deokhugam.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeit.team4.deokhugam.book.entity.Book;
import com.codeit.team4.deokhugam.book.repository.BookRepository;
import com.codeit.team4.deokhugam.config.TestContainerConfig;
import com.codeit.team4.deokhugam.dashboard.entity.PeriodType;
import com.codeit.team4.deokhugam.dashboard.entity.PopularBook;
import com.codeit.team4.deokhugam.dashboard.repository.PopularBookRepository;
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
class DashboardBatchServiceTest {

    @Autowired
    private DashboardBatchService dashboardBatchService;

    @Autowired
    private PopularBookRepository popularBookRepository;

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

    @Nested
    @DisplayName("인기 도서 배치")
    class UpdatePopularBooks {

        @Test
        @DisplayName("인기 도서 배치 실행 성공")
        void updatePopularBooks_success() {
            Book book1 = createBook("책1", "1111111111");
            Book book2 = createBook("책2", "2222222222");
            reviewRepository.saveAndFlush(new Review(book1, user, "좋아요", 5));
            reviewRepository.saveAndFlush(new Review(book2, user, "괜찮아요", 3));

            LocalDate today = LocalDate.now();
            dashboardBatchService.updatePopularBooks(today);

            List<PopularBook> results = popularBookRepository.findAll();
            assertThat(results).isNotEmpty();
        }

        @Test
        @DisplayName("LIMIT 4 적용 성공")
        void updatePopularBooks_limit_success() {
            for (int i = 0; i < 6; i++) {
                Book book = createBook("책" + i, "100000000" + i);
                reviewRepository.saveAndFlush(new Review(book, user, "리뷰" + i, 5));
            }

            LocalDate today = LocalDate.now();
            dashboardBatchService.updatePopularBooks(today);

            for (PeriodType period : PeriodType.values()) {
                List<PopularBook> byPeriod = popularBookRepository.findAll().stream()
                        .filter(pb -> pb.getPeriod() == period && pb.getSnapshotDate().equals(today))
                        .toList();
                assertThat(byPeriod).hasSizeLessThanOrEqualTo(4);
            }
        }

        @Test
        @DisplayName("ranking 순서 부여 성공")
        void updatePopularBooks_ranking_success() {
            Book book1 = createBook("인기책", "1111111111");
            Book book2 = createBook("보통책", "2222222222");
            reviewRepository.saveAndFlush(new Review(book1, user, "좋아요", 5));
            reviewRepository.saveAndFlush(new Review(book2, user, "그냥그래요", 1));

            LocalDate today = LocalDate.now();
            dashboardBatchService.updatePopularBooks(today);

            List<PopularBook> dailyBooks = popularBookRepository.findAll().stream()
                    .filter(pb -> pb.getPeriod() == PeriodType.DAILY && pb.getSnapshotDate().equals(today))
                    .sorted((a, b) -> Integer.compare(a.getRanking(), b.getRanking()))
                    .toList();

            assertThat(dailyBooks).hasSize(2);
            assertThat(dailyBooks.get(0).getRanking()).isEqualTo(1);
            assertThat(dailyBooks.get(1).getRanking()).isEqualTo(2);
            assertThat(dailyBooks.get(0).getScore()).isGreaterThanOrEqualTo(dailyBooks.get(1).getScore());
        }

        @Test
        @DisplayName("리뷰 없을 때 빈 결과 반환 성공")
        void updatePopularBooks_noReviews_success() {
            LocalDate today = LocalDate.now();
            dashboardBatchService.updatePopularBooks(today);

            assertThat(popularBookRepository.findAll()).isEmpty();
        }

        @Test
        @DisplayName("모든 PeriodType에 대해 배치 실행 성공")
        void updatePopularBooks_allPeriods_success() {
            Book book = createBook("책1", "1111111111");
            reviewRepository.saveAndFlush(new Review(book, user, "좋아요", 5));

            LocalDate today = LocalDate.now();
            dashboardBatchService.updatePopularBooks(today);

            for (PeriodType period : PeriodType.values()) {
                List<PopularBook> byPeriod = popularBookRepository.findAll().stream()
                        .filter(pb -> pb.getPeriod() == period && pb.getSnapshotDate().equals(today))
                        .toList();
                assertThat(byPeriod).isNotEmpty();
            }
        }
    }
}
