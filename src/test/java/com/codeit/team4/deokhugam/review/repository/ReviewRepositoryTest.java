package com.codeit.team4.deokhugam.review.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeit.team4.deokhugam.book.entity.Book;
import com.codeit.team4.deokhugam.book.repository.BookRepository;
import com.codeit.team4.deokhugam.config.TestContainerConfig;
import com.codeit.team4.deokhugam.global.config.JpaAuditingConfig;
import com.codeit.team4.deokhugam.review.entity.Review;
import com.codeit.team4.deokhugam.user.entity.User;
import com.codeit.team4.deokhugam.user.repository.UserRepository;
import com.codeit.team4.deokhugam.comment.entity.Comment;
import com.codeit.team4.deokhugam.comment.repository.CommentRepository;
import com.codeit.team4.deokhugam.review.entity.ReviewLike;
import java.time.LocalDate;
import java.util.Optional;
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
class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewLikeRepository reviewLikeRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User user;
    private Book book;

    @BeforeEach
    void setUp() {
        user = userRepository.save(new User("test@test.com", "테스터", "password123"));
        book = bookRepository.save(new Book("클린 코드", "로버트 마틴", "좋은 책", "출판사", LocalDate.of(2024, 1, 1), "1234567890", null));
    }

    @Nested
    @DisplayName("중복 리뷰 존재 확인")
    class ExistsByBookIdAndUserIdAndDeletedAtIsNull {

        @Test
        @DisplayName("리뷰가 존재하면 true 반환 성공")
        void exists_returnsTrue() {
            reviewRepository.save(new Review(book, user, "좋은 책입니다", 5));

            boolean exists = reviewRepository.existsByBookIdAndUserIdAndDeletedAtIsNull(
                    book.getId(), user.getId());

            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("리뷰가 없으면 false 반환 성공")
        void notExists_returnsFalse() {
            boolean exists = reviewRepository.existsByBookIdAndUserIdAndDeletedAtIsNull(
                    book.getId(), user.getId());

            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("소프트 삭제된 리뷰는 false 반환 성공")
        void softDeleted_returnsFalse() {
            Review review = reviewRepository.save(new Review(book, user, "좋은 책입니다", 5));

            entityManager.getEntityManager()
                    .createQuery("UPDATE Review r SET r.deletedAt = CURRENT_TIMESTAMP WHERE r.id = :id")
                    .setParameter("id", review.getId())
                    .executeUpdate();
            entityManager.flush();
            entityManager.clear();

            boolean exists = reviewRepository.existsByBookIdAndUserIdAndDeletedAtIsNull(
                    book.getId(), user.getId());

            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("리뷰 ID로 조회 (soft delete 제외)")
    class FindByIdAndDeletedAtIsNull {

        @Test
        @DisplayName("리뷰 조회 성공")
        void findById_success() {
            Review saved = reviewRepository.save(new Review(book, user, "좋은 책입니다", 5));

            Optional<Review> result = reviewRepository.findByIdAndDeletedAtIsNull(saved.getId());

            assertThat(result).isPresent();
            assertThat(result.get().getContent()).isEqualTo("좋은 책입니다");
        }

        @Test
        @DisplayName("소프트 삭제된 리뷰는 조회 실패")
        void findById_softDeleted_fail() {
            Review review = reviewRepository.save(new Review(book, user, "좋은 책입니다", 5));

            entityManager.getEntityManager()
                    .createQuery("UPDATE Review r SET r.deletedAt = CURRENT_TIMESTAMP WHERE r.id = :id")
                    .setParameter("id", review.getId())
                    .executeUpdate();
            entityManager.flush();
            entityManager.clear();

            Optional<Review> result = reviewRepository.findByIdAndDeletedAtIsNull(review.getId());

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("소프트 삭제된 리뷰 findById 조회")
    class FindWithDeletedById {

        @Test
        @DisplayName("소프트 삭제된 리뷰도 findById로 조회 성공")
        void findById_softDeleted_success() {
            Review review = reviewRepository.save(new Review(book, user, "좋은 책입니다", 5));

            entityManager.getEntityManager()
                    .createQuery("UPDATE Review r SET r.deletedAt = CURRENT_TIMESTAMP WHERE r.id = :id")
                    .setParameter("id", review.getId())
                    .executeUpdate();
            entityManager.flush();
            entityManager.clear();

            Optional<Review> result = reviewRepository.findById(review.getId());

            assertThat(result).isPresent();
            assertThat(result.get().getContent()).isEqualTo("좋은 책입니다");
        }
    }

    @Nested
    @DisplayName("좋아요 수 증가")
    class IncreaseLikeCount {

        @Test
        @DisplayName("좋아요 수 증가 성공")
        void increaseLikeCount_success() {
            Review review = reviewRepository.save(new Review(book, user, "좋은 책입니다", 5));

            reviewRepository.increaseLikeCount(review.getId());
            entityManager.clear();

            Review found = reviewRepository.findById(review.getId()).get();
            assertThat(found.getLikeCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("좋아요 수 연속 증가 성공")
        void increaseLikeCount_multiple_success() {
            Review review = reviewRepository.save(new Review(book, user, "좋은 책입니다", 5));

            reviewRepository.increaseLikeCount(review.getId());
            reviewRepository.increaseLikeCount(review.getId());
            entityManager.clear();

            Review found = reviewRepository.findById(review.getId()).get();
            assertThat(found.getLikeCount()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("좋아요 수 감소")
    class DecreaseLikeCount {

        @Test
        @DisplayName("좋아요 수 감소 성공")
        void decreaseLikeCount_success() {
            Review review = reviewRepository.save(new Review(book, user, "좋은 책입니다", 5));
            reviewRepository.increaseLikeCount(review.getId());
            entityManager.clear();

            reviewRepository.decreaseLikeCount(review.getId());
            entityManager.clear();

            Review found = reviewRepository.findById(review.getId()).get();
            assertThat(found.getLikeCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("좋아요 수 0 이하로 감소하지 않음 성공")
        void decreaseLikeCount_notBelowZero_success() {
            Review review = reviewRepository.save(new Review(book, user, "좋은 책입니다", 5));

            reviewRepository.decreaseLikeCount(review.getId());
            entityManager.clear();

            Review found = reviewRepository.findById(review.getId()).get();
            assertThat(found.getLikeCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("댓글 수 감소")
    class DecreaseCommentCount {
        @Test
        @DisplayName("댓글 수 감소 성공")
        void decreaseCommentCount_Decreases_ActualDBValue() {
            // given
            Review review = reviewRepository.saveAndFlush(new Review(book, user, "리뷰 내용", 5));
            reviewRepository.increaseCommentCount(review.getId());
            reviewRepository.increaseCommentCount(review.getId());

            // when
            reviewRepository.decreaseCommentCount(review.getId());

            // then
            entityManager.clear();
            Review updatedReview = reviewRepository.findById(review.getId()).get();

            assertThat(updatedReview.getCommentCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("댓글 수 0 이하로 감소하지 않음 성공")
        void decreaseCommentCount_notBelowZero_success() {
            Review review = reviewRepository.saveAndFlush(new Review(book, user, "content", 5));
            reviewRepository.decreaseCommentCount(review.getId());
            entityManager.clear();

            Review found = reviewRepository.findById(review.getId()).get();
            assertThat(found.getCommentCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("리뷰 물리 삭제 시 연관 객체 CASCADE 삭제")
    class HardDeleteCascade {
        @Test
        @DisplayName("리뷰 삭제 시 좋아요도 함께 삭제 성공")
        void hardDelete_cascadeDeletesReviewLikes() {
            Review review = reviewRepository.save(new Review(book, user, "좋은 책입니다", 5));
            ReviewLike like = new ReviewLike(review, user);
            entityManager.persist(like);
            entityManager.flush();
            entityManager.clear();

            reviewRepository.deleteById(review.getId());
            entityManager.flush();
            entityManager.clear();

            assertThat(reviewRepository.findById(review.getId())).isEmpty();
            assertThat(reviewLikeRepository.findById(like.getId())).isEmpty();
        }

        @Test
        @DisplayName("리뷰 삭제 시 댓글도 함께 삭제 성공")
        void hardDelete_cascadeDeletesComments() {
            Review review = reviewRepository.save(new Review(book, user, "좋은 책입니다", 5));
            Comment comment = new Comment(user, review, "좋은 리뷰입니다");
            entityManager.persist(comment);
            entityManager.flush();
            entityManager.clear();

            reviewRepository.deleteById(review.getId());
            entityManager.flush();
            entityManager.clear();

            assertThat(reviewRepository.findById(review.getId())).isEmpty();
            assertThat(commentRepository.findById(comment.getId())).isEmpty();
        }

        @Test
        @DisplayName("리뷰 소프트 삭제 시 댓글은 유지 성공")
        void softDelete_preservesComments() {
            Review review = reviewRepository.save(new Review(book, user, "좋은 책입니다", 5));
            Comment comment = new Comment(user, review, "좋은 리뷰입니다");
            entityManager.persist(comment);
            entityManager.flush();
            entityManager.clear();

            entityManager.getEntityManager()
                    .createQuery("UPDATE Review r SET r.deletedAt = CURRENT_TIMESTAMP WHERE r.id = :id")
                    .setParameter("id", review.getId())
                    .executeUpdate();
            entityManager.flush();
            entityManager.clear();

            assertThat(reviewRepository.findById(review.getId())).isPresent();
            assertThat(reviewRepository.findByIdAndDeletedAtIsNull(review.getId())).isEmpty();
            assertThat(commentRepository.findById(comment.getId())).isPresent();
        }
    }
}
