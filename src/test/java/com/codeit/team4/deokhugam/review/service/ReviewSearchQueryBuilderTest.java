package com.codeit.team4.deokhugam.review.service;

import static com.codeit.team4.deokhugam.jooq.tables.Books.BOOKS;
import static com.codeit.team4.deokhugam.jooq.tables.Reviews.REVIEWS;
import static com.codeit.team4.deokhugam.jooq.tables.Users.USERS;
import static org.assertj.core.api.Assertions.assertThat;

import com.codeit.team4.deokhugam.book.entity.Book;
import com.codeit.team4.deokhugam.book.repository.BookRepository;
import com.codeit.team4.deokhugam.config.TestContainerConfig;
import com.codeit.team4.deokhugam.global.response.SortDirection;
import com.codeit.team4.deokhugam.review.dto.ReviewOrderBy;
import com.codeit.team4.deokhugam.review.dto.ReviewSearchRequestParam;
import com.codeit.team4.deokhugam.review.entity.Review;
import com.codeit.team4.deokhugam.review.repository.ReviewRepository;
import com.codeit.team4.deokhugam.user.entity.User;
import com.codeit.team4.deokhugam.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.SortField;
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
class ReviewSearchQueryBuilderTest {

    @Autowired
    private ReviewSearchQueryBuilder queryBuilder;

    @Autowired
    private DSLContext dsl;

    @Autowired
    private ReviewRepository reviewRepository;

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

    private int countWithCondition(Condition condition) {
        return dsl.selectCount()
                .from(REVIEWS)
                .join(BOOKS).on(REVIEWS.BOOK_ID.eq(BOOKS.ID))
                .join(USERS).on(REVIEWS.USER_ID.eq(USERS.ID))
                .where(condition)
                .fetchOne(0, int.class);
    }

    @Nested
    @DisplayName("필터 조건")
    class FilterCondition {

        @Test
        @DisplayName("소프트 삭제된 리뷰 제외 성공")
        void buildCondition_excludesSoftDeleted_success() {
            Review review = reviewRepository.saveAndFlush(new Review(book, user, "좋은 책입니다", 5));
            review.softDelete();
            reviewRepository.saveAndFlush(review);

            ReviewSearchRequestParam param = new ReviewSearchRequestParam(
                    null, null, null, ReviewOrderBy.CREATED_AT, SortDirection.DESC,
                    null, null, 50, user.getId()
            );

            Condition condition = queryBuilder.buildCondition(param);

            assertThat(countWithCondition(condition)).isZero();
        }

        @Test
        @DisplayName("작성자 ID 필터링 성공")
        void buildCondition_filterByUserId_success() {
            reviewRepository.saveAndFlush(new Review(book, user, "좋은 책입니다", 5));
            reviewRepository.saveAndFlush(new Review(otherBook, otherUser, "괜찮습니다", 3));

            ReviewSearchRequestParam param = new ReviewSearchRequestParam(
                    user.getId(), null, null, ReviewOrderBy.CREATED_AT, SortDirection.DESC,
                    null, null, 50, user.getId()
            );

            Condition condition = queryBuilder.buildCondition(param);

            assertThat(countWithCondition(condition)).isEqualTo(1);
        }

        @Test
        @DisplayName("도서 ID 필터링 성공")
        void buildCondition_filterByBookId_success() {
            reviewRepository.saveAndFlush(new Review(book, user, "좋은 책입니다", 5));
            reviewRepository.saveAndFlush(new Review(otherBook, otherUser, "괜찮습니다", 3));

            ReviewSearchRequestParam param = new ReviewSearchRequestParam(
                    null, book.getId(), null, ReviewOrderBy.CREATED_AT, SortDirection.DESC,
                    null, null, 50, user.getId()
            );

            Condition condition = queryBuilder.buildCondition(param);

            assertThat(countWithCondition(condition)).isEqualTo(1);
        }

        @Test
        @DisplayName("키워드로 내용 검색 성공")
        void buildCondition_filterByKeywordContent_success() {
            reviewRepository.saveAndFlush(new Review(book, user, "좋은 책입니다", 5));
            reviewRepository.saveAndFlush(new Review(otherBook, otherUser, "별로입니다", 2));

            ReviewSearchRequestParam param = new ReviewSearchRequestParam(
                    null, null, "좋은", ReviewOrderBy.CREATED_AT, SortDirection.DESC,
                    null, null, 50, user.getId()
            );

            Condition condition = queryBuilder.buildCondition(param);

            assertThat(countWithCondition(condition)).isEqualTo(1);
        }

        @Test
        @DisplayName("키워드로 도서 제목 검색 성공")
        void buildCondition_filterByKeywordBookTitle_success() {
            reviewRepository.saveAndFlush(new Review(book, user, "좋은 책입니다", 5));
            reviewRepository.saveAndFlush(new Review(otherBook, otherUser, "별로입니다", 2));

            ReviewSearchRequestParam param = new ReviewSearchRequestParam(
                    null, null, "클린", ReviewOrderBy.CREATED_AT, SortDirection.DESC,
                    null, null, 50, user.getId()
            );

            Condition condition = queryBuilder.buildCondition(param);

            assertThat(countWithCondition(condition)).isEqualTo(1);
        }

        @Test
        @DisplayName("키워드로 작성자 닉네임 검색 성공")
        void buildCondition_filterByKeywordNickname_success() {
            reviewRepository.saveAndFlush(new Review(book, user, "좋은 책입니다", 5));
            reviewRepository.saveAndFlush(new Review(otherBook, otherUser, "별로입니다", 2));

            ReviewSearchRequestParam param = new ReviewSearchRequestParam(
                    null, null, "테스터", ReviewOrderBy.CREATED_AT, SortDirection.DESC,
                    null, null, 50, user.getId()
            );

            Condition condition = queryBuilder.buildCondition(param);

            assertThat(countWithCondition(condition)).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("정렬 조건")
    class OrderByCondition {

        @Test
        @DisplayName("createdAt DESC 정렬 성공")
        void buildOrderBy_createdAtDesc_success() {
            ReviewSearchRequestParam param = new ReviewSearchRequestParam(
                    null, null, null, ReviewOrderBy.CREATED_AT, SortDirection.DESC,
                    null, null, 50, user.getId()
            );

            List<SortField<?>> orderBy = queryBuilder.buildOrderBy(param);

            assertThat(orderBy).hasSize(2);
        }

        @Test
        @DisplayName("rating DESC 정렬 시 필드 3개 성공")
        void buildOrderBy_ratingDesc_success() {
            ReviewSearchRequestParam param = new ReviewSearchRequestParam(
                    null, null, null, ReviewOrderBy.RATING, SortDirection.DESC,
                    null, null, 50, user.getId()
            );

            List<SortField<?>> orderBy = queryBuilder.buildOrderBy(param);

            assertThat(orderBy).hasSize(3);
        }
    }
}
