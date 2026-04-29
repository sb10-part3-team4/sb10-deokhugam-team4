package com.codeit.team4.deokhugam.dashboard.builder;

import static com.codeit.team4.deokhugam.jooq.tables.Books.BOOKS;
import static com.codeit.team4.deokhugam.jooq.tables.Reviews.REVIEWS;
import static com.codeit.team4.deokhugam.jooq.tables.Users.USERS;
import static org.assertj.core.api.Assertions.assertThat;

import com.codeit.team4.deokhugam.book.entity.Book;
import com.codeit.team4.deokhugam.book.repository.BookRepository;
import com.codeit.team4.deokhugam.config.TestContainerConfig;
import com.codeit.team4.deokhugam.dashboard.entity.PeriodType;
import com.codeit.team4.deokhugam.review.entity.Review;
import com.codeit.team4.deokhugam.review.repository.ReviewRepository;
import com.codeit.team4.deokhugam.user.entity.User;
import com.codeit.team4.deokhugam.user.repository.UserRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.jooq.Condition;
import org.jooq.DSLContext;
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
class PopularReviewAggregationQueryBuilderTest {

    @Autowired
    private PopularReviewAggregationQueryBuilder queryBuilder;

    @Autowired
    private DSLContext dsl;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Value("${dashboard.batch.zone}")
    private String zone;

    private User user;
    private Book book;

    @BeforeEach
    void setUp() {
        user = userRepository.saveAndFlush(new User("test@test.com", "테스터", "password123"));
        book = bookRepository.saveAndFlush(new Book("클린 코드", "로버트 마틴", "좋은 책", "출판사", LocalDate.of(2024, 1, 1), "1234567890", null));
    }

    private int countWithCondition(Condition condition) {
        return dsl.selectCount()
                .from(REVIEWS)
                .join(BOOKS).on(REVIEWS.BOOK_ID.eq(BOOKS.ID))
                .join(USERS).on(REVIEWS.USER_ID.eq(USERS.ID))
                .where(condition)
                .fetchOne(0, int.class);
    }

    @Nested
    @DisplayName("기간 조건")
    class PeriodCondition {

        @Test
        @DisplayName("DAILY 조건 생성 성공")
        void buildCondition_daily_success() {
            reviewRepository.saveAndFlush(new Review(book, user, "좋은 책입니다", 5));

            LocalDate today = LocalDate.now(ZoneId.of(zone));
            Condition condition = queryBuilder.buildCondition(PeriodType.DAILY, today);

            assertThat(countWithCondition(condition)).isEqualTo(1);
        }

        @Test
        @DisplayName("WEEKLY 고정 기간 조건 생성 성공")
        void buildCondition_weekly_success() {
            reviewRepository.saveAndFlush(new Review(book, user, "좋은 책입니다", 5));

            LocalDate today = LocalDate.now(ZoneId.of(zone));
            Condition condition = queryBuilder.buildCondition(PeriodType.WEEKLY, today);

            assertThat(countWithCondition(condition)).isEqualTo(1);
        }

        @Test
        @DisplayName("MONTHLY 고정 기간 조건 생성 성공")
        void buildCondition_monthly_success() {
            reviewRepository.saveAndFlush(new Review(book, user, "좋은 책입니다", 5));

            LocalDate today = LocalDate.now(ZoneId.of(zone));
            Condition condition = queryBuilder.buildCondition(PeriodType.MONTHLY, today);

            assertThat(countWithCondition(condition)).isEqualTo(1);
        }

        @Test
        @DisplayName("ALL_TIME 조건 생성 성공")
        void buildCondition_allTime_success() {
            reviewRepository.saveAndFlush(new Review(book, user, "좋은 책입니다", 5));

            LocalDate today = LocalDate.now(ZoneId.of(zone));
            Condition condition = queryBuilder.buildCondition(PeriodType.ALL_TIME, today);

            assertThat(countWithCondition(condition)).isEqualTo(1);
        }

        @Test
        @DisplayName("ALL_TIME은 과거 리뷰도 포함 성공")
        void buildCondition_allTime_includesPastReviews_success() {
            reviewRepository.saveAndFlush(new Review(book, user, "좋은 책입니다", 5));

            LocalDate today = LocalDate.now(ZoneId.of(zone));

            Condition dailyCondition = queryBuilder.buildCondition(PeriodType.DAILY, today.minusDays(30));
            Condition allTimeCondition = queryBuilder.buildCondition(PeriodType.ALL_TIME, today);

            assertThat(countWithCondition(dailyCondition)).isZero();
            assertThat(countWithCondition(allTimeCondition)).isEqualTo(1);
        }

        @Test
        @DisplayName("소프트 삭제된 리뷰 제외 성공")
        void buildCondition_excludesSoftDeleted_success() {
            Review review = reviewRepository.saveAndFlush(new Review(book, user, "좋은 책입니다", 5));
            review.softDelete();
            reviewRepository.saveAndFlush(review);

            LocalDate today = LocalDate.now(ZoneId.of(zone));
            Condition condition = queryBuilder.buildCondition(PeriodType.ALL_TIME, today);

            assertThat(countWithCondition(condition)).isZero();
        }

        @Test
        @DisplayName("소프트 삭제된 도서 제외 성공")
        void buildCondition_excludesSoftDeletedBook_success() {
            reviewRepository.saveAndFlush(new Review(book, user, "좋은 책입니다", 5));
            book.softDelete(Instant.now());
            bookRepository.saveAndFlush(book);

            LocalDate today = LocalDate.now(ZoneId.of(zone));
            Condition condition = queryBuilder.buildCondition(PeriodType.ALL_TIME, today);

            assertThat(countWithCondition(condition)).isZero();
        }
    }

    @Nested
    @DisplayName("정렬 조건")
    class OrderByCondition {

        @Test
        @DisplayName("정렬 조건 3개 생성 성공")
        void buildOrderBy_hasThreeFields_success() {
            assertThat(queryBuilder.buildOrderBy()).hasSize(3);
        }

        @Test
        @DisplayName("동점 리뷰 정렬 순서 일관성 성공")
        void buildOrderBy_tieBreaker_success() {
            Book otherBook = bookRepository.saveAndFlush(
                    new Book("다른 책", "다른 저자", "설명", "출판사", LocalDate.of(2024, 2, 1), "1234567891", null)
            );
            reviewRepository.saveAndFlush(new Review(book, user, "좋은 책입니다", 5));
            reviewRepository.saveAndFlush(new Review(otherBook, user, "좋은 책입니다", 5));

            LocalDate today = LocalDate.now(ZoneId.of(zone));
            Condition condition = queryBuilder.buildCondition(PeriodType.ALL_TIME, today);

            List<UUID> firstResult = dsl.select(REVIEWS.ID)
                    .from(REVIEWS)
                    .join(BOOKS).on(REVIEWS.BOOK_ID.eq(BOOKS.ID))
                    .join(USERS).on(REVIEWS.USER_ID.eq(USERS.ID))
                    .where(condition)
                    .orderBy(queryBuilder.buildOrderBy())
                    .fetch(REVIEWS.ID);

            List<UUID> secondResult = dsl.select(REVIEWS.ID)
                    .from(REVIEWS)
                    .join(BOOKS).on(REVIEWS.BOOK_ID.eq(BOOKS.ID))
                    .join(USERS).on(REVIEWS.USER_ID.eq(USERS.ID))
                    .where(condition)
                    .orderBy(queryBuilder.buildOrderBy())
                    .fetch(REVIEWS.ID);

            assertThat(firstResult).hasSize(2);
            assertThat(firstResult).isEqualTo(secondResult);
        }

        @Test
        @DisplayName("동점 시 먼저 작성한 리뷰가 우선 성공")
        void buildOrderBy_tieBreaker_earlierReviewFirst_success() {
            Review earlierReview = reviewRepository.saveAndFlush(new Review(book, user, "먼저 쓴 리뷰", 5));

            Book otherBook = bookRepository.saveAndFlush(
                    new Book("다른 책", "다른 저자", "설명", "출판사", LocalDate.of(2024, 2, 1), "1234567891", null)
            );
            User otherUser = userRepository.saveAndFlush(new User("other@test.com", "다른사람", "password123"));
            Review laterReview = reviewRepository.saveAndFlush(new Review(otherBook, otherUser, "나중에 쓴 리뷰", 5));

            LocalDate today = LocalDate.now(ZoneId.of(zone));
            Condition condition = queryBuilder.buildCondition(PeriodType.ALL_TIME, today);

            List<UUID> result = dsl.select(REVIEWS.ID)
                    .from(REVIEWS)
                    .join(BOOKS).on(REVIEWS.BOOK_ID.eq(BOOKS.ID))
                    .join(USERS).on(REVIEWS.USER_ID.eq(USERS.ID))
                    .where(condition)
                    .orderBy(queryBuilder.buildOrderBy())
                    .fetch(REVIEWS.ID);

            assertThat(result).hasSize(2);
            assertThat(result.get(0)).isEqualTo(earlierReview.getId());
            assertThat(result.get(1)).isEqualTo(laterReview.getId());
        }
    }
}
