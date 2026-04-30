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
import com.codeit.team4.deokhugam.review.entity.ReviewStatistics;
import com.codeit.team4.deokhugam.review.repository.ReviewStatisticsRepository;
import org.jooq.DSLContext;
import static com.codeit.team4.deokhugam.jooq.tables.Comments.COMMENTS;
import static com.codeit.team4.deokhugam.jooq.tables.ReviewLikes.REVIEW_LIKES;
import static com.codeit.team4.deokhugam.jooq.tables.Reviews.REVIEWS;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.UUID;
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
    private ReviewStatisticsRepository reviewStatisticsRepository;

    @Autowired
    private DSLContext dsl;

    @Value("${dashboard.batch.zone}")
    private String zone;

    private User user;

    @BeforeEach
    void setUp() {
        user = userRepository.saveAndFlush(new User("test@test.com", "테스터", "password123"));
    }

    private void saveReviewStatistics(java.util.UUID reviewId, int commentCount) {
        ReviewStatistics stats = new ReviewStatistics(reviewId);
        for (int i = 0; i < commentCount; i++) {
            stats.onCommentCreated();
        }
        reviewStatisticsRepository.saveAndFlush(stats);
    }

    private void insertReviewLikes(UUID reviewId, UUID userId, int count) {
        for (int i = 0; i < count; i++) {
            User likeUser = userRepository.saveAndFlush(
                    new User("like" + UUID.randomUUID() + "@test.com", "좋아요유저", "password123")
            );
            dsl.insertInto(REVIEW_LIKES)
                    .set(REVIEW_LIKES.REVIEW_ID, reviewId)
                    .set(REVIEW_LIKES.USER_ID, likeUser.getId())
                    .set(REVIEW_LIKES.CREATED_AT, todayTime())
                    .execute();
        }
    }

    private void insertComments(UUID reviewId, UUID userId, int count) {
        for (int i = 0; i < count; i++) {
            dsl.insertInto(COMMENTS)
                    .set(COMMENTS.REVIEW_ID, reviewId)
                    .set(COMMENTS.USER_ID, userId)
                    .set(COMMENTS.CONTENT, "댓글 " + i)
                    .set(COMMENTS.CREATED_AT, todayTime())
                    .set(COMMENTS.UPDATED_AT, todayTime())
                    .execute();
        }
    }

    private void insertReviewLikesAt(UUID reviewId, int count, OffsetDateTime createdAt) {
        for (int i = 0; i < count; i++) {
            User likeUser = userRepository.saveAndFlush(
                    new User("like" + UUID.randomUUID() + "@test.com", "좋아요유저", "password123")
            );
            dsl.insertInto(REVIEW_LIKES)
                    .set(REVIEW_LIKES.REVIEW_ID, reviewId)
                    .set(REVIEW_LIKES.USER_ID, likeUser.getId())
                    .set(REVIEW_LIKES.CREATED_AT, createdAt)
                    .execute();
        }
    }

    private void insertCommentsAt(UUID reviewId, UUID userId, int count, OffsetDateTime createdAt) {
        for (int i = 0; i < count; i++) {
            dsl.insertInto(COMMENTS)
                    .set(COMMENTS.REVIEW_ID, reviewId)
                    .set(COMMENTS.USER_ID, userId)
                    .set(COMMENTS.CONTENT, "댓글 " + i)
                    .set(COMMENTS.CREATED_AT, createdAt)
                    .set(COMMENTS.UPDATED_AT, createdAt)
                    .execute();
        }
    }

    private OffsetDateTime todayTime() {
        return LocalDate.now(ZoneId.of(zone)).atStartOfDay(ZoneId.of(zone)).toOffsetDateTime().plusHours(12);
    }

    private OffsetDateTime daysAgo(int days) {
        return todayTime().minusDays(days);
    }

    private Book createBook(String title, String isbn) {
        return bookRepository.saveAndFlush(
                new Book(title, "저자", "설명", "출판사", LocalDate.of(2024, 1, 1), isbn, null)
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

        @Test
        @DisplayName("DAILY 기간 외 리뷰는 점수에 미포함 성공")
        void updatePopularBooks_dailyPeriodFilter_success() {
            Book book = createBook("기간 테스트 책", "9999999999");

            // 오늘 리뷰 1개 (rating=5)
            reviewRepository.saveAndFlush(new Review(book, user, "오늘 리뷰", 5));

            // 어제 리뷰 1개 (rating=1) → DAILY에서는 제외되어야 함
            User otherUser = userRepository.saveAndFlush(new User("other@test.com", "다른유저", "password123"));
            Review oldReview = reviewRepository.saveAndFlush(new Review(book, otherUser, "어제 리뷰", 1));
            dsl.update(REVIEWS).set(REVIEWS.CREATED_AT, daysAgo(1))
                    .where(REVIEWS.ID.eq(oldReview.getId())).execute();

            LocalDate today = LocalDate.now(ZoneId.of(zone));
            dashboardBatchService.updatePopularBooks(today);

            PopularBook dailyBook = popularBookRepository.findAll().stream()
                    .filter(pb -> pb.getPeriod() == PeriodType.DAILY && pb.getSnapshotDate().equals(today))
                    .findFirst().orElseThrow();

            // DAILY: 오늘 리뷰 1개만 포함 → reviewCount=1, rating=5.00
            assertThat(dailyBook.getReviewCount()).isEqualTo(1);
            assertThat(dailyBook.getRating()).isEqualByComparingTo(new BigDecimal("5.00"));

            // 점수 = 1 * 0.4 + 5.00 * 0.6 = 3.4000
            BigDecimal expectedScore = new BigDecimal("1").multiply(PopularBook.REVIEW_COUNT_WEIGHT)
                    .add(new BigDecimal("5.00").multiply(PopularBook.AVG_RATING_WEIGHT))
                    .setScale(4, RoundingMode.HALF_UP);
            assertThat(dailyBook.getScore()).isEqualByComparingTo(expectedScore);

            // ALL_TIME: 전체 리뷰 2개 포함
            PopularBook allTimeBook = popularBookRepository.findAll().stream()
                    .filter(pb -> pb.getPeriod() == PeriodType.ALL_TIME && pb.getSnapshotDate().equals(today))
                    .findFirst().orElseThrow();

            assertThat(allTimeBook.getReviewCount()).isEqualTo(2);
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
            insertReviewLikes(review1.getId(), user.getId(), 10);
            insertComments(review1.getId(), user.getId(), 5);
            insertReviewLikes(review2.getId(), user.getId(), 2);
            insertComments(review2.getId(), user.getId(), 1);

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
        @DisplayName("DAILY 기간 외 좋아요/댓글은 count와 점수에 미포함 성공")
        void updatePopularReviews_dailyPeriodFilter_success() {
            Book book = createBook("기간 테스트 책", "9999999999");
            Review review = reviewRepository.saveAndFlush(new Review(book, user, "테스트 리뷰", 5));

            // 오늘 좋아요 3개, 오늘 댓글 2개
            insertReviewLikesAt(review.getId(), 3, todayTime());
            insertCommentsAt(review.getId(), user.getId(), 2, todayTime());

            // 어제 좋아요 10개, 어제 댓글 5개 → DAILY에서 제외
            insertReviewLikesAt(review.getId(), 10, daysAgo(1));
            insertCommentsAt(review.getId(), user.getId(), 5, daysAgo(1));

            LocalDate today = LocalDate.now(ZoneId.of(zone));
            dashboardBatchService.updatePopularReviews(today);

            PopularReview dailyReview = popularReviewRepository.findAll().stream()
                    .filter(pr -> pr.getPeriod() == PeriodType.DAILY && pr.getSnapshotDate().equals(today))
                    .findFirst().orElseThrow();

            // DAILY: 오늘 좋아요 3개, 오늘 댓글 2개만 포함
            assertThat(dailyReview.getLikeCount()).isEqualTo(3);
            assertThat(dailyReview.getCommentCount()).isEqualTo(2);

            // 점수 = 3 * 0.3 + 2 * 0.7 = 2.3000
            BigDecimal expectedScore = new BigDecimal("3").multiply(PopularReview.LIKE_COUNT_WEIGHT)
                    .add(new BigDecimal("2").multiply(PopularReview.COMMENT_COUNT_WEIGHT))
                    .setScale(4, RoundingMode.HALF_UP);
            assertThat(dailyReview.getScore()).isEqualByComparingTo(expectedScore);

            // ALL_TIME: 전체 좋아요 13개, 전체 댓글 7개 포함
            PopularReview allTimeReview = popularReviewRepository.findAll().stream()
                    .filter(pr -> pr.getPeriod() == PeriodType.ALL_TIME && pr.getSnapshotDate().equals(today))
                    .findFirst().orElseThrow();

            assertThat(allTimeReview.getLikeCount()).isEqualTo(13);
            assertThat(allTimeReview.getCommentCount()).isEqualTo(7);
        }

        @Test
        @DisplayName("삭제된 댓글은 기간 내 댓글 수에 미포함 성공")
        void updatePopularReviews_deletedCommentExcluded_success() {
            Book book = createBook("삭제 댓글 테스트", "8888888888");
            Review review = reviewRepository.saveAndFlush(new Review(book, user, "테스트 리뷰", 5));

            // 오늘 댓글 3개 (정상 2개 + 삭제 1개)
            insertCommentsAt(review.getId(), user.getId(), 2, todayTime());
            dsl.insertInto(COMMENTS)
                    .set(COMMENTS.REVIEW_ID, review.getId())
                    .set(COMMENTS.USER_ID, user.getId())
                    .set(COMMENTS.CONTENT, "삭제된 댓글")
                    .set(COMMENTS.CREATED_AT, todayTime())
                    .set(COMMENTS.UPDATED_AT, todayTime())
                    .set(COMMENTS.DELETED_AT, todayTime())
                    .execute();

            LocalDate today = LocalDate.now(ZoneId.of(zone));
            dashboardBatchService.updatePopularReviews(today);

            PopularReview dailyReview = popularReviewRepository.findAll().stream()
                    .filter(pr -> pr.getPeriod() == PeriodType.DAILY && pr.getSnapshotDate().equals(today))
                    .findFirst().orElseThrow();

            assertThat(dailyReview.getCommentCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("DAILY 기간 경계 시각 직전 데이터는 미포함 성공")
        void updatePopularReviews_boundaryTime_success() {
            Book book = createBook("경계 테스트 책", "7777777777");
            Review review = reviewRepository.saveAndFlush(new Review(book, user, "테스트 리뷰", 5));

            LocalDate today = LocalDate.now(ZoneId.of(zone));
            OffsetDateTime startOfToday = today.atStartOfDay(ZoneId.of(zone)).toOffsetDateTime();

            // 오늘 시작 직전 (어제 23:59:59) → DAILY에서 제외
            insertReviewLikesAt(review.getId(), 5, startOfToday.minusSeconds(1));
            // 오늘 시작 시각 → DAILY에 포함
            insertReviewLikesAt(review.getId(), 2, startOfToday);

            dashboardBatchService.updatePopularReviews(today);

            PopularReview dailyReview = popularReviewRepository.findAll().stream()
                    .filter(pr -> pr.getPeriod() == PeriodType.DAILY && pr.getSnapshotDate().equals(today))
                    .findFirst().orElseThrow();

            assertThat(dailyReview.getLikeCount()).isEqualTo(2);
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
                assertThat(byPeriod).hasSize(10);
            }
        }

        @Test
        @DisplayName("score 기반 ranking 순서 부여 성공")
        void updatePowerUsers_ranking_success() {
            User otherUser = userRepository.saveAndFlush(new User("other@test.com", "다른사람", "password123"));
            Book book1 = createBook("책1", "1111111111");
            Book book2 = createBook("책2", "2222222222");
            Book book3 = createBook("책3", "3333333333");
            Review review1 = reviewRepository.saveAndFlush(new Review(book1, user, "좋아요", 5));
            Review review2 = reviewRepository.saveAndFlush(new Review(book2, user, "또 좋아요", 5));
            Review review3 = reviewRepository.saveAndFlush(new Review(book3, otherUser, "괜찮아요", 3));

            // user 리뷰: like=10, comment=5 각각 → 활동 점수 높음
            insertReviewLikes(review1.getId(), user.getId(), 10);
            insertComments(review1.getId(), user.getId(), 5);
            insertReviewLikes(review2.getId(), user.getId(), 8);
            insertComments(review2.getId(), user.getId(), 4);
            // otherUser 리뷰: like=1, comment=0 → 활동 점수 낮음
            insertReviewLikes(review3.getId(), otherUser.getId(), 1);

            LocalDate today = LocalDate.now(ZoneId.of(zone));
            dashboardBatchService.updatePowerUsers(today);

            List<PowerUser> dailyUsers = powerUserRepository.findAll().stream()
                    .filter(pu -> pu.getPeriod() == PeriodType.DAILY && pu.getSnapshotDate().equals(today))
                    .sorted((a, b) -> Integer.compare(a.getRank(), b.getRank()))
                    .toList();

            assertThat(dailyUsers).hasSize(2);
            assertThat(dailyUsers.get(0).getRank()).isEqualTo(1);
            assertThat(dailyUsers.get(1).getRank()).isEqualTo(2);
            assertThat(dailyUsers.get(0).getScore()).isGreaterThan(dailyUsers.get(1).getScore());
        }

        @Test
        @DisplayName("DAILY 기간 외 좋아요/댓글은 count와 점수에 미포함 성공")
        void updatePowerUsers_dailyPeriodFilter_success() {
            Book book = createBook("기간 테스트 책", "9999999999");
            Review review = reviewRepository.saveAndFlush(new Review(book, user, "테스트 리뷰", 5));

            // 오늘 좋아요 4개, 오늘 댓글 3개
            insertReviewLikesAt(review.getId(), 4, todayTime());
            insertCommentsAt(review.getId(), user.getId(), 3, todayTime());

            // 어제 좋아요 20개, 어제 댓글 10개 → DAILY에서 제외
            insertReviewLikesAt(review.getId(), 20, daysAgo(1));
            insertCommentsAt(review.getId(), user.getId(), 10, daysAgo(1));

            LocalDate today = LocalDate.now(ZoneId.of(zone));
            dashboardBatchService.updatePowerUsers(today);

            PowerUser dailyUser = powerUserRepository.findAll().stream()
                    .filter(pu -> pu.getPeriod() == PeriodType.DAILY && pu.getSnapshotDate().equals(today))
                    .findFirst().orElseThrow();

            // DAILY: 오늘 좋아요 4개, 오늘 댓글 3개만 포함
            assertThat(dailyUser.getLikeCount()).isEqualTo(4);
            assertThat(dailyUser.getCommentCount()).isEqualTo(3);

            // ALL_TIME: 전체 좋아요 24개, 전체 댓글 13개 포함
            PowerUser allTimeUser = powerUserRepository.findAll().stream()
                    .filter(pu -> pu.getPeriod() == PeriodType.ALL_TIME && pu.getSnapshotDate().equals(today))
                    .findFirst().orElseThrow();

            assertThat(allTimeUser.getLikeCount()).isEqualTo(24);
            assertThat(allTimeUser.getCommentCount()).isEqualTo(13);
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
