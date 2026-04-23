package com.codeit.team4.deokhugam.notification.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.codeit.team4.deokhugam.book.entity.Book;
import com.codeit.team4.deokhugam.book.repository.BookRepository;
import com.codeit.team4.deokhugam.config.TestContainerConfig;
import com.codeit.team4.deokhugam.global.config.JpaAuditingConfig;
import com.codeit.team4.deokhugam.notification.entity.Notification;
import com.codeit.team4.deokhugam.review.entity.Review;
import com.codeit.team4.deokhugam.review.repository.ReviewRepository;
import com.codeit.team4.deokhugam.user.entity.User;
import com.codeit.team4.deokhugam.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({TestContainerConfig.class, JpaAuditingConfig.class})
class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private BookRepository bookRepository;

    @Test
    @DisplayName("알림 ID와 userId가 일치하면 조회 성공")
    void findByIdAndUserId_success() {
        // given
        User user = createDummyUser();
        Book book = createDummyBook();
        Review review = createDummyReview(user, book);

        Notification notification = new Notification(
                user.getId(),
                review.getId(),
                "content",
                "message"
        );

        Notification saved = notificationRepository.save(notification);

        // when
        Optional<Notification> result =
                notificationRepository.findByIdAndUserId(saved.getId(), saved.getUserId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getUserId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("다른 userId로 조회하면 조회 실패")
    void findByIdAndUserId_fail() {
        // given
        User user = createDummyUser();
        Book book = createDummyBook();
        Review review = createDummyReview(user, book);

        Notification notification = new Notification(
                user.getId(),
                review.getId(),
                "content",
                "message"
        );

        Notification saved = notificationRepository.save(notification);

        // when
        Optional<Notification> result =
                notificationRepository.findByIdAndUserId(saved.getId(), UUID.randomUUID());

        // then
        assertThat(result).isEmpty();
    }

    // 헬퍼 메서드
    private User createDummyUser() {
        return userRepository.save(
                new User(
                        "test-" + UUID.randomUUID() + "@test.com",
                        "testUser",
                        "password1!"
                )
        );
    }

    private Book createDummyBook() {
        Book book = new Book(
                "test book",
                "author",
                "description",
                "publisher",
                LocalDate.now(),
                "isbn-" + UUID.randomUUID()
        );
        return bookRepository.save(book);
    }

    private Review createDummyReview(User user, Book book) {
        Review review = new Review(
                book,
                user,
                "review content",
                5
        );
        return reviewRepository.save(review);
    }
}