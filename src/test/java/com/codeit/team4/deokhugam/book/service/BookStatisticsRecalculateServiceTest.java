package com.codeit.team4.deokhugam.book.service;

import static com.codeit.team4.deokhugam.jooq.tables.BookStatistics.BOOK_STATISTICS;
import static org.assertj.core.api.Assertions.assertThat;

import com.codeit.team4.deokhugam.book.entity.Book;
import com.codeit.team4.deokhugam.book.entity.BookStatistics;
import com.codeit.team4.deokhugam.book.repository.BookRepository;
import com.codeit.team4.deokhugam.book.repository.BookStatisticsRepository;
import com.codeit.team4.deokhugam.config.TestContainerConfig;
import com.codeit.team4.deokhugam.review.entity.Review;
import com.codeit.team4.deokhugam.review.repository.ReviewRepository;
import com.codeit.team4.deokhugam.user.entity.User;
import com.codeit.team4.deokhugam.user.repository.UserRepository;
import java.time.LocalDate;
import jakarta.persistence.EntityManager;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
class BookStatisticsRecalculateServiceTest {

    @Autowired
    private BookStatisticsRecalculateService recalculateService;

    @Autowired
    private BookStatisticsRepository bookStatisticsRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DSLContext dsl;

    @Autowired
    private EntityManager entityManager;

    private User user1;
    private User user2;
    private Book book1;
    private Book book2;

    @BeforeEach
    void setUp() {
        dsl.deleteFrom(BOOK_STATISTICS).execute();

        user1 = userRepository.saveAndFlush(
                new User("user1@test.com", "유저1", "Test1234!")
        );
        user2 = userRepository.saveAndFlush(
                new User("user2@test.com", "유저2", "Test1234!")
        );
        book1 = bookRepository.saveAndFlush(
                new Book("도서1", "저자1", "설명1", "출판사1", LocalDate.of(2024, 1, 1), "9781234567891")
        );
        book2 = bookRepository.saveAndFlush(
                new Book("도서2", "저자2", "설명2", "출판사2", LocalDate.of(2024, 1, 1), "9781234567892")
        );
    }

    @Test
    @DisplayName("통계 없는 상태에서 재계산 성공")
    void recalculateBookStatisticsFromReviews_noExistingStatistics_success() {
        reviewRepository.saveAndFlush(new Review(book1, user1, "리뷰1", 5));
        reviewRepository.saveAndFlush(new Review(book1, user2, "리뷰2", 3));
        reviewRepository.saveAndFlush(new Review(book2, user1, "리뷰3", 4));

        int rows = recalculateService.recalculateBookStatisticsFromReviews();

        assertThat(rows).isEqualTo(2);

        BookStatistics stats1 = bookStatisticsRepository.findById(book1.getId()).orElseThrow();
        assertThat(stats1.getReviewCount()).isEqualTo(2);
        assertThat(stats1.getRatingSum()).isEqualTo(8);

        BookStatistics stats2 = bookStatisticsRepository.findById(book2.getId()).orElseThrow();
        assertThat(stats2.getReviewCount()).isEqualTo(1);
        assertThat(stats2.getRatingSum()).isEqualTo(4);
    }

    @Test
    @DisplayName("틀어진 통계 재계산으로 보정 성공")
    void recalculateBookStatisticsFromReviews_driftedStatistics_success() {
        reviewRepository.saveAndFlush(new Review(book1, user1, "리뷰1", 5));
        reviewRepository.saveAndFlush(new Review(book1, user2, "리뷰2", 3));

        BookStatistics drifted = new BookStatistics(book1.getId());
        drifted.onReviewCreated(5);
        bookStatisticsRepository.saveAndFlush(drifted);
        // 현재 상태: count=1, sum=5 (실제: count=2, sum=8)

        recalculateService.recalculateBookStatisticsFromReviews();
        entityManager.clear();

        BookStatistics corrected = bookStatisticsRepository.findById(book1.getId()).orElseThrow();
        assertThat(corrected.getReviewCount()).isEqualTo(2);
        assertThat(corrected.getRatingSum()).isEqualTo(8);
    }

    @Test
    @DisplayName("소프트삭제된 리뷰 제외하고 재계산 성공")
    void recalculateBookStatisticsFromReviews_excludesSoftDeleted_success() {
        Review review1 = reviewRepository.saveAndFlush(new Review(book1, user1, "리뷰1", 5));
        reviewRepository.saveAndFlush(new Review(book1, user2, "리뷰2", 3));
        review1.softDelete();
        reviewRepository.saveAndFlush(review1);

        recalculateService.recalculateBookStatisticsFromReviews();

        BookStatistics stats = bookStatisticsRepository.findById(book1.getId()).orElseThrow();
        assertThat(stats.getReviewCount()).isEqualTo(1);
        assertThat(stats.getRatingSum()).isEqualTo(3);
    }

    @Test
    @DisplayName("리뷰 없으면 재계산 0건 성공")
    void recalculateBookStatisticsFromReviews_noReviews_success() {
        int rows = recalculateService.recalculateBookStatisticsFromReviews();

        assertThat(rows).isZero();
    }
}
