package com.codeit.team4.deokhugam.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeit.team4.deokhugam.book.entity.Book;
import com.codeit.team4.deokhugam.book.repository.BookRepository;
import com.codeit.team4.deokhugam.config.TestContainerConfig;
import com.codeit.team4.deokhugam.dashboard.entity.PeriodType;
import com.codeit.team4.deokhugam.dashboard.entity.PopularBook;
import com.codeit.team4.deokhugam.dashboard.entity.PopularReview;
import com.codeit.team4.deokhugam.dashboard.entity.PowerUser;
import com.codeit.team4.deokhugam.dashboard.repository.PopularBookRepository;
import com.codeit.team4.deokhugam.dashboard.repository.PopularReviewRepository;
import com.codeit.team4.deokhugam.dashboard.repository.PowerUserRepository;
import com.codeit.team4.deokhugam.review.entity.Review;
import com.codeit.team4.deokhugam.review.repository.ReviewRepository;
import com.codeit.team4.deokhugam.user.entity.User;
import com.codeit.team4.deokhugam.user.repository.UserRepository;
import org.jooq.DSLContext;
import static com.codeit.team4.deokhugam.jooq.tables.Reviews.REVIEWS;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
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
class DashboardBatchServiceTest {

    @Autowired
    private DashboardBatchService dashboardBatchService;

    @Autowired
    private PopularBookRepository popularBookRepository;

    @Autowired
    private PopularReviewRepository popularReviewRepository;

    @Autowired
    private PowerUserRepository powerUserRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private DSLContext dsl;

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

            LocalDate today = LocalDate.now(ZoneId.of(zone));
            dashboardBatchService.updatePopularBooks(today);

            List<PopularBook> results = popularBookRepository.findAll();
            assertThat(results).isNotEmpty();
        }

        @Test
        @DisplayName("LIMIT 4 적용 성공")
        void updatePopularBooks_limit_success() {
            for (int i = 0; i < 6; i++) {
                Book book = createBook("책" + i, "100000000" + i);
                reviewRepository.saveAndFlush(new Review(book, user, "리뷰" + i, (i % 5) + 1));
            }

            LocalDate today = LocalDate.now(ZoneId.of(zone));
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

            LocalDate today = LocalDate.now(ZoneId.of(zone));
            dashboardBatchService.updatePopularBooks(today);

            List<PopularBook> dailyBooks = popularBookRepository.findAll().stream()
                    .filter(pb -> pb.getPeriod() == PeriodType.DAILY && pb.getSnapshotDate().equals(today))
                    .sorted((a, b) -> Integer.compare(a.getRank(), b.getRank()))
                    .toList();

            assertThat(dailyBooks).hasSize(2);
            assertThat(dailyBooks.get(0).getRank()).isEqualTo(1);
            assertThat(dailyBooks.get(1).getRank()).isEqualTo(2);
            assertThat(dailyBooks.get(0).getScore()).isGreaterThanOrEqualTo(dailyBooks.get(1).getScore());
        }

        @Test
        @DisplayName("리뷰 없을 때 빈 결과 반환 성공")
        void updatePopularBooks_noReviews_success() {
            LocalDate today = LocalDate.now(ZoneId.of(zone));
            dashboardBatchService.updatePopularBooks(today);

            assertThat(popularBookRepository.findAll()).isEmpty();
        }

        @Test
        @DisplayName("모든 PeriodType에 대해 배치 실행 성공")
        void updatePopularBooks_allPeriods_success() {
            Book book = createBook("책1", "1111111111");
            reviewRepository.saveAndFlush(new Review(book, user, "좋아요", 5));

            LocalDate today = LocalDate.now(ZoneId.of(zone));
            dashboardBatchService.updatePopularBooks(today);

            for (PeriodType period : PeriodType.values()) {
                List<PopularBook> byPeriod = popularBookRepository.findAll().stream()
                        .filter(pb -> pb.getPeriod() == period && pb.getSnapshotDate().equals(today))
                        .toList();
                assertThat(byPeriod).isNotEmpty();
            }
        }
    }

    @Nested
    @DisplayName("인기 리뷰 배치")
    class UpdatePopularReviews {

        @Test
        @DisplayName("인기 리뷰 배치 실행 성공")
        void updatePopularReviews_success() {
            Book book1 = createBook("책1", "1111111111");
            Book book2 = createBook("책2", "2222222222");
            reviewRepository.saveAndFlush(new Review(book1, user, "좋아요", 5));
            reviewRepository.saveAndFlush(new Review(book2, user, "괜찮아요", 3));

            LocalDate today = LocalDate.now(ZoneId.of(zone));
            dashboardBatchService.updatePopularReviews(today);

            List<PopularReview> results = popularReviewRepository.findAll();
            assertThat(results).isNotEmpty();
        }

        @Test
        @DisplayName("LIMIT 20 적용 성공")
        void updatePopularReviews_limit_success() {
            for (int i = 0; i < 25; i++) {
                Book book = createBook("책" + i, "100000000" + String.format("%02d", i));
                reviewRepository.saveAndFlush(new Review(book, user, "리뷰" + i, (i % 5) + 1));
            }

            LocalDate today = LocalDate.now(ZoneId.of(zone));
            dashboardBatchService.updatePopularReviews(today);

            for (PeriodType period : PeriodType.values()) {
                List<PopularReview> byPeriod = popularReviewRepository.findAll().stream()
                        .filter(pr -> pr.getPeriod() == period && pr.getSnapshotDate().equals(today))
                        .toList();
                assertThat(byPeriod).hasSize(20);
            }
        }

        @Test
        @DisplayName("score 기반 ranking 순서 부여 성공")
        void updatePopularReviews_ranking_success() {
            Book book1 = createBook("책1", "1111111111");
            Book book2 = createBook("책2", "2222222222");
            Review review1 = reviewRepository.saveAndFlush(new Review(book1, user, "인기 리뷰", 5));
            Review review2 = reviewRepository.saveAndFlush(new Review(book2, user, "보통 리뷰", 3));

            // review1: like=10, comment=5 → score = 10*0.3 + 5*0.7 = 6.5
            // review2: like=2, comment=1 → score = 2*0.3 + 1*0.7 = 1.3
            dsl.update(REVIEWS).set(REVIEWS.LIKE_COUNT, 10).set(REVIEWS.COMMENT_COUNT, 5)
                    .where(REVIEWS.ID.eq(review1.getId())).execute();
            dsl.update(REVIEWS).set(REVIEWS.LIKE_COUNT, 2).set(REVIEWS.COMMENT_COUNT, 1)
                    .where(REVIEWS.ID.eq(review2.getId())).execute();

            LocalDate today = LocalDate.now(ZoneId.of(zone));
            dashboardBatchService.updatePopularReviews(today);

            List<PopularReview> dailyReviews = popularReviewRepository.findAll().stream()
                    .filter(pr -> pr.getPeriod() == PeriodType.DAILY && pr.getSnapshotDate().equals(today))
                    .sorted((a, b) -> Integer.compare(a.getRank(), b.getRank()))
                    .toList();

            assertThat(dailyReviews).hasSize(2);
            assertThat(dailyReviews.get(0).getRank()).isEqualTo(1);
            assertThat(dailyReviews.get(1).getRank()).isEqualTo(2);
            assertThat(dailyReviews.get(0).getScore()).isGreaterThan(dailyReviews.get(1).getScore());
        }

        @Test
        @DisplayName("리뷰 없을 때 빈 결과 반환 성공")
        void updatePopularReviews_noReviews_success() {
            LocalDate today = LocalDate.now(ZoneId.of(zone));
            dashboardBatchService.updatePopularReviews(today);

            assertThat(popularReviewRepository.findAll()).isEmpty();
        }

        @Test
        @DisplayName("모든 PeriodType에 대해 배치 실행 성공")
        void updatePopularReviews_allPeriods_success() {
            Book book = createBook("책1", "1111111111");
            reviewRepository.saveAndFlush(new Review(book, user, "좋아요", 5));

            LocalDate today = LocalDate.now(ZoneId.of(zone));
            dashboardBatchService.updatePopularReviews(today);

            for (PeriodType period : PeriodType.values()) {
                List<PopularReview> byPeriod = popularReviewRepository.findAll().stream()
                        .filter(pr -> pr.getPeriod() == period && pr.getSnapshotDate().equals(today))
                        .toList();
                assertThat(byPeriod).isNotEmpty();
            }
        }
    }

    @Nested
    @DisplayName("파워 유저 배치")
    class UpdatePowerUsers {

        @Test
        @DisplayName("파워 유저 배치 실행 성공")
        void updatePowerUsers_success() {
            Book book1 = createBook("책1", "1111111111");
            reviewRepository.saveAndFlush(new Review(book1, user, "좋아요", 5));

            LocalDate today = LocalDate.now(ZoneId.of(zone));
            dashboardBatchService.updatePowerUsers(today);

            List<PowerUser> results = powerUserRepository.findAll();
            assertThat(results).isNotEmpty();
        }

        @Test
        @DisplayName("LIMIT 10 적용 성공")
        void updatePowerUsers_limit_success() {
            for (int i = 0; i < 15; i++) {
                User u = userRepository.saveAndFlush(new User("user" + i + "@test.com", "유저" + i, "password123"));
                Book b = createBook("책" + i, "100000000" + String.format("%02d", i));
                reviewRepository.saveAndFlush(new Review(b, u, "리뷰" + i, (i % 5) + 1));
            }

            LocalDate today = LocalDate.now(ZoneId.of(zone));
            dashboardBatchService.updatePowerUsers(today);

            for (PeriodType period : PeriodType.values()) {
                List<PowerUser> byPeriod = powerUserRepository.findAll().stream()
                        .filter(pu -> pu.getPeriod() == period && pu.getSnapshotDate().equals(today))
                        .toList();
                assertThat(byPeriod).hasSizeLessThanOrEqualTo(10);
            }
        }

        @Test
        @DisplayName("ranking 순서 부여 성공")
        void updatePowerUsers_ranking_success() {
            User otherUser = userRepository.saveAndFlush(new User("other@test.com", "다른사람", "password123"));
            Book book1 = createBook("책1", "1111111111");
            Book book2 = createBook("책2", "2222222222");
            Book book3 = createBook("책3", "3333333333");
            // user가 리뷰 2개 → 점수 높음
            reviewRepository.saveAndFlush(new Review(book1, user, "좋아요", 5));
            reviewRepository.saveAndFlush(new Review(book2, user, "또 좋아요", 5));
            // otherUser가 리뷰 1개 → 점수 낮음
            reviewRepository.saveAndFlush(new Review(book3, otherUser, "괜찮아요", 3));

            LocalDate today = LocalDate.now(ZoneId.of(zone));
            dashboardBatchService.updatePowerUsers(today);

            List<PowerUser> dailyUsers = powerUserRepository.findAll().stream()
                    .filter(pu -> pu.getPeriod() == PeriodType.DAILY && pu.getSnapshotDate().equals(today))
                    .sorted((a, b) -> Integer.compare(a.getRank(), b.getRank()))
                    .toList();

            assertThat(dailyUsers).hasSize(2);
            assertThat(dailyUsers.get(0).getRank()).isEqualTo(1);
            assertThat(dailyUsers.get(1).getRank()).isEqualTo(2);
            assertThat(dailyUsers.get(0).getScore()).isGreaterThanOrEqualTo(dailyUsers.get(1).getScore());
        }

        @Test
        @DisplayName("리뷰 없을 때 빈 결과 반환 성공")
        void updatePowerUsers_noReviews_success() {
            LocalDate today = LocalDate.now(ZoneId.of(zone));
            dashboardBatchService.updatePowerUsers(today);

            assertThat(powerUserRepository.findAll()).isEmpty();
        }

        @Test
        @DisplayName("모든 PeriodType에 대해 배치 실행 성공")
        void updatePowerUsers_allPeriods_success() {
            Book book1 = createBook("책1", "1111111111");
            reviewRepository.saveAndFlush(new Review(book1, user, "좋아요", 5));

            LocalDate today = LocalDate.now(ZoneId.of(zone));
            dashboardBatchService.updatePowerUsers(today);

            for (PeriodType period : PeriodType.values()) {
                List<PowerUser> byPeriod = powerUserRepository.findAll().stream()
                        .filter(pu -> pu.getPeriod() == period && pu.getSnapshotDate().equals(today))
                        .toList();
                assertThat(byPeriod).isNotEmpty();
            }
        }
    }
}
