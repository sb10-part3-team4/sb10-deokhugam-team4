package com.codeit.team4.deokhugam.review.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeit.team4.deokhugam.book.entity.Book;
import com.codeit.team4.deokhugam.book.repository.BookRepository;
import com.codeit.team4.deokhugam.config.TestContainerConfig;
import com.codeit.team4.deokhugam.global.config.JpaAuditingConfig;
import com.codeit.team4.deokhugam.review.entity.Review;
import com.codeit.team4.deokhugam.user.entity.User;
import com.codeit.team4.deokhugam.user.repository.UserRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import({TestContainerConfig.class, JpaAuditingConfig.class})
@ActiveProfiles("test")
class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Test
    @DisplayName("같은 도서에 같은 사용자의 리뷰 존재 확인 성공")
    void test1() {
        User user = userRepository.save(new User("test@test.com", "테스터", "password123"));
        Book book = bookRepository.save(new Book("클린 코드", "로버트 마틴", "좋은 책", "출판사", LocalDate.of(2024, 1, 1), "1234567890"));
        reviewRepository.save(new Review(book, user, "좋은 책입니다", 5));

        boolean exists = reviewRepository.existsByBookIdAndUserIdAndDeletedAtIsNull(
                book.getId(), user.getId());

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("같은 도서에 같은 사용자의 리뷰 미존재 확인 성공")
    void test2() {
        User user = userRepository.save(new User("test@test.com", "테스터", "password123"));
        Book book = bookRepository.save(new Book("클린 코드", "로버트 마틴", "좋은 책", "출판사", LocalDate.of(2024, 1, 1), "1234567890"));

        boolean exists = reviewRepository.existsByBookIdAndUserIdAndDeletedAtIsNull(
                book.getId(), user.getId());

        assertThat(exists).isFalse();
    }
}
