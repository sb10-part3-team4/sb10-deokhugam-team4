package com.codeit.team4.deokhugam.dashboard.book;

import static com.codeit.team4.deokhugam.jooq.tables.Books.BOOKS;
import static com.codeit.team4.deokhugam.jooq.tables.Reviews.REVIEWS;
import static org.assertj.core.api.Assertions.assertThat;

import com.codeit.team4.deokhugam.book.entity.Book;
import com.codeit.team4.deokhugam.book.repository.BookRepository;
import com.codeit.team4.deokhugam.config.TestContainerConfig;
import com.codeit.team4.deokhugam.dashboard.entity.PeriodType;
import com.codeit.team4.deokhugam.review.entity.Review;
import com.codeit.team4.deokhugam.review.repository.ReviewRepository;
import com.codeit.team4.deokhugam.user.entity.User;
import com.codeit.team4.deokhugam.user.repository.UserRepository;
import java.time.LocalDate;
import org.jooq.Condition;
import org.jooq.DSLContext;
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
class PopularBookQueryBuilderTest {

    @Autowired
    private PopularBookQueryBuilder queryBuilder;

    @Autowired
    private DSLContext dsl;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    private User user;
    private Book book;

    @BeforeEach
    void setUp() {
        user = userRepository.saveAndFlush(new User("test@test.com", "테스터", "password123"));
        book = bookRepository.saveAndFlush(new Book("클린 코드", "로버트 마틴", "좋은 책", "출판사", LocalDate.of(2024, 1, 1), "1234567890"));
    }

    private int countWithCondition(Condition condition) {
        return dsl.selectCount()
                .from(REVIEWS)
                .join(BOOKS).on(REVIEWS.BOOK_ID.eq(BOOKS.ID))
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

            LocalDate today = LocalDate.now();
            Condition condition = queryBuilder.buildCondition(PeriodType.DAILY, today);

            assertThat(countWithCondition(condition)).isEqualTo(1);
        }

        @Test
        @DisplayName("WEEKLY 고정 기간 조건 생성 성공")
        void buildCondition_weekly_success() {
            reviewRepository.saveAndFlush(new Review(book, user, "좋은 책입니다", 5));

            LocalDate today = LocalDate.now();
            Condition condition = queryBuilder.buildCondition(PeriodType.WEEKLY, today);

            assertThat(countWithCondition(condition)).isEqualTo(1);
        }

        @Test
        @DisplayName("MONTHLY 고정 기간 조건 생성 성공")
        void buildCondition_monthly_success() {
            reviewRepository.saveAndFlush(new Review(book, user, "좋은 책입니다", 5));

            LocalDate today = LocalDate.now();
            Condition condition = queryBuilder.buildCondition(PeriodType.MONTHLY, today);

            assertThat(countWithCondition(condition)).isEqualTo(1);
        }

        @Test
        @DisplayName("ALL_TIME 조건 생성 성공")
        void buildCondition_allTime_success() {
            reviewRepository.saveAndFlush(new Review(book, user, "좋은 책입니다", 5));

            LocalDate today = LocalDate.now();
            Condition condition = queryBuilder.buildCondition(PeriodType.ALL_TIME, today);

            assertThat(countWithCondition(condition)).isEqualTo(1);
        }

        @Test
        @DisplayName("소프트 삭제된 리뷰 제외 성공")
        void buildCondition_excludesSoftDeleted_success() {
            Review review = reviewRepository.saveAndFlush(new Review(book, user, "좋은 책입니다", 5));
            review.softDelete();
            reviewRepository.saveAndFlush(review);

            LocalDate today = LocalDate.now();
            Condition condition = queryBuilder.buildCondition(PeriodType.ALL_TIME, today);

            assertThat(countWithCondition(condition)).isZero();
        }

        @Test
        @DisplayName("기간 외 리뷰 제외 성공")
        void buildCondition_excludesOutOfRange_success() {
            reviewRepository.saveAndFlush(new Review(book, user, "좋은 책입니다", 5));

            LocalDate pastDate = LocalDate.of(2020, 1, 1);
            Condition condition = queryBuilder.buildCondition(PeriodType.DAILY, pastDate);

            assertThat(countWithCondition(condition)).isZero();
        }
    }

    @Nested
    @DisplayName("정렬 조건")
    class OrderByCondition {

        @Test
        @DisplayName("정렬 조건 생성 성공")
        void buildOrderBy_success() {
            assertThat(queryBuilder.buildOrderBy()).isNotNull();
        }
    }
}
