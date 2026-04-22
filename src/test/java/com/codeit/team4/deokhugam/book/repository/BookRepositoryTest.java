package com.codeit.team4.deokhugam.book.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeit.team4.deokhugam.book.entity.Book;
import com.codeit.team4.deokhugam.config.TestContainerConfig;
import com.codeit.team4.deokhugam.global.config.JpaAuditingConfig;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import({TestContainerConfig.class, JpaAuditingConfig.class})
@ActiveProfiles("test")
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Book book;

    @BeforeEach
    void setUp() {
        book = bookRepository.save(
                new Book("클린 코드", "로버트 마틴", "좋은 책", "출판사",
                        LocalDate.of(2024, 1, 1), "1234567890")
        );
        entityManager.flush();
        entityManager.clear();
    }

    @Nested
    @DisplayName("리뷰 수 증가")
    class IncreaseReviewCount {

        @Test
        @DisplayName("리뷰 수 증가 성공")
        void increaseReviewCount_success() {
            bookRepository.increaseReviewCount(book.getId());
            entityManager.flush();
            entityManager.clear();

            Book found = bookRepository.findById(book.getId()).orElseThrow();
            assertThat(found.getReviewCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("리뷰 수 여러 번 증가 성공")
        void increaseReviewCount_multiple_success() {
            bookRepository.increaseReviewCount(book.getId());
            bookRepository.increaseReviewCount(book.getId());
            bookRepository.increaseReviewCount(book.getId());
            entityManager.flush();
            entityManager.clear();

            Book found = bookRepository.findById(book.getId()).orElseThrow();
            assertThat(found.getReviewCount()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("리뷰 수 감소")
    class DecreaseReviewCount {

        @Test
        @DisplayName("리뷰 수 감소 성공")
        void decreaseReviewCount_success() {
            bookRepository.increaseReviewCount(book.getId());
            entityManager.flush();
            entityManager.clear();

            bookRepository.decreaseReviewCount(book.getId());
            entityManager.flush();
            entityManager.clear();

            Book found = bookRepository.findById(book.getId()).orElseThrow();
            assertThat(found.getReviewCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("리뷰 수 0 이하로 감소하지 않음")
        void decreaseReviewCount_notBelowZero_success() {
            bookRepository.decreaseReviewCount(book.getId());
            entityManager.flush();
            entityManager.clear();

            Book found = bookRepository.findById(book.getId()).orElseThrow();
            assertThat(found.getReviewCount()).isEqualTo(0);
        }
    }
}
