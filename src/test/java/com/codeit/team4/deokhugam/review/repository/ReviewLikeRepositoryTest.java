package com.codeit.team4.deokhugam.review.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeit.team4.deokhugam.book.entity.Book;
import com.codeit.team4.deokhugam.book.repository.BookRepository;
import com.codeit.team4.deokhugam.config.TestContainerConfig;
import com.codeit.team4.deokhugam.global.config.JpaAuditingConfig;
import com.codeit.team4.deokhugam.review.entity.Review;
import com.codeit.team4.deokhugam.review.entity.ReviewLike;
import com.codeit.team4.deokhugam.user.entity.User;
import com.codeit.team4.deokhugam.user.repository.UserRepository;
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
class ReviewLikeRepositoryTest {

    @Autowired
    private ReviewLikeRepository reviewLikeRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User user;
    private Review review;

    @BeforeEach
    void setUp() {
        user = userRepository.save(new User("test@test.com", "테스터", "password123"));
        Book book = bookRepository.save(new Book("클린 코드", "로버트 마틴", "좋은 책", "출판사", LocalDate.of(2024, 1, 1), "1234567890", null));
        review = reviewRepository.save(new Review(book, user, "좋은 책입니다", 5));
    }

    @Nested
    @DisplayName("좋아요 존재 확인")
    class ExistsByReviewIdAndUserId {

        @Test
        @DisplayName("좋아요가 존재하면 true 반환 성공")
        void exists_returnsTrue() {
            reviewLikeRepository.save(new ReviewLike(review, user));

            boolean exists = reviewLikeRepository.existsByReviewIdAndUserId(
                    review.getId(), user.getId());

            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("좋아요가 없으면 false 반환 성공")
        void notExists_returnsFalse() {
            boolean exists = reviewLikeRepository.existsByReviewIdAndUserId(
                    review.getId(), user.getId());

            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("다른 사용자의 좋아요는 false 반환 성공")
        void otherUser_returnsFalse() {
            User otherUser = userRepository.save(new User("other@test.com", "다른사람", "password123"));
            reviewLikeRepository.save(new ReviewLike(review, otherUser));

            boolean exists = reviewLikeRepository.existsByReviewIdAndUserId(
                    review.getId(), user.getId());

            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("좋아요 삭제")
    class DeleteByReviewIdAndUserId {

        @Test
        @DisplayName("좋아요 삭제 성공")
        void deleteByReviewIdAndUserId_success() {
            reviewLikeRepository.save(new ReviewLike(review, user));
            entityManager.flush();
            entityManager.clear();

            reviewLikeRepository.deleteByReviewIdAndUserId(review.getId(), user.getId());
            entityManager.flush();
            entityManager.clear();

            boolean exists = reviewLikeRepository.existsByReviewIdAndUserId(
                    review.getId(), user.getId());
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("좋아요가 없어도 삭제 시 에러 없음 성공")
        void deleteByReviewIdAndUserId_noLike_success() {
            reviewLikeRepository.deleteByReviewIdAndUserId(review.getId(), user.getId());
            entityManager.flush();

            boolean exists = reviewLikeRepository.existsByReviewIdAndUserId(
                    review.getId(), user.getId());
            assertThat(exists).isFalse();
        }
    }
}
