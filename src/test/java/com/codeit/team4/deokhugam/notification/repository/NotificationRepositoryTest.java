package com.codeit.team4.deokhugam.notification.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeit.team4.deokhugam.book.entity.Book;
import com.codeit.team4.deokhugam.book.repository.BookRepository;
import com.codeit.team4.deokhugam.config.TestContainerConfig;
import com.codeit.team4.deokhugam.global.config.JpaAuditingConfig;
import com.codeit.team4.deokhugam.notification.entity.Notification;
import com.codeit.team4.deokhugam.review.entity.Review;
import com.codeit.team4.deokhugam.review.repository.ReviewRepository;
import com.codeit.team4.deokhugam.user.entity.User;
import com.codeit.team4.deokhugam.user.repository.UserRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
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

    @Autowired
    private TestEntityManager em;

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

    @Test
    @DisplayName("읽지 않은 알림이 존재할 때 userId로 조회하면 성공")
    void findByUserIdAndConfirmedFalse_whenUnreadExists_thenSuccess() {

        // given
        User user = createDummyUser();
        Book book = createDummyBook();
        Review review = createDummyReview(user, book);

        Notification unread1 = notificationRepository.save(
                new Notification(user.getId(), review.getId(), "content1", "message1")
        );

        Notification unread2 = notificationRepository.save(
                new Notification(user.getId(), review.getId(), "content2", "message2")
        );

        Notification read = notificationRepository.save(
                new Notification(user.getId(), review.getId(), "content3", "message3")
        );
        read.markAsRead();

        // when
        List<Notification> result =
                notificationRepository.findByUserIdAndConfirmedFalse(user.getId());

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Notification::getId)
                .containsExactlyInAnyOrder(unread1.getId(), unread2.getId());
    }

    @Test
    @DisplayName("모든 알림이 읽음 상태일 때 조회하면 빈 결과가 반환된다")
    void findByUserIdAndConfirmedFalse_whenAllRead_thenEmpty() {

        // given
        User user = createDummyUser();
        Book book = createDummyBook();
        Review review = createDummyReview(user, book);

        Notification read = notificationRepository.save(
                new Notification(user.getId(), review.getId(), "content", "message")
        );
        read.markAsRead();

        // when
        List<Notification> result =
                notificationRepository.findByUserIdAndConfirmedFalse(user.getId());

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("다른 userId의 알림을 조회하면 필터링되어 조회 실패")
    void findByUserIdAndConfirmedFalse_whenOtherUser_thenFail() {

        // given
        User user1 = createDummyUser();
        User user2 = createDummyUser();

        Book book = createDummyBook();
        Review review1 = createDummyReview(user1, book);
        Review review2 = createDummyReview(user2, book);

        notificationRepository.save(
                new Notification(user1.getId(), review1.getId(), "content1", "message1")
        );

        notificationRepository.save(
                new Notification(user2.getId(), review2.getId(), "content2", "message2")
        );

        // when
        List<Notification> result =
                notificationRepository.findByUserIdAndConfirmedFalse(user1.getId());

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(user1.getId());
    }

    @Test
    @DisplayName("읽음 상태이고 7일 이전 알림을 삭제하면 성공")
    void deleteOldReadNotifications_success() {

        // given
        User user = createDummyUser();
        Book book = createDummyBook();
        Review review = createDummyReview(user, book);

        Notification notification = notificationRepository.save(
                new Notification(user.getId(), review.getId(), "content", "message")
        );

        notification.markAsRead();
        notificationRepository.save(notification);

        // createdAt을 직접 제어할 수 없어 현재 시점 기준으로 threshold를 미래로 설정하여 삭제 조건을 보장함
        Instant threshold = Instant.now().plusSeconds(1);

        // when
        int deletedCount = notificationRepository.deleteOldReadNotifications(threshold);

        em.flush();
        em.clear();

        // then
        assertThat(deletedCount).isEqualTo(1);
        assertThat(notificationRepository.findById(notification.getId()))
                .isEmpty();
    }

    @Test
    @DisplayName("읽지 않은 알림은 삭제되지 않아서 성공")
    void deleteOldReadNotifications_whenUnread_thenNotDeleted() {

        // given
        User user = createDummyUser();
        Book book = createDummyBook();
        Review review = createDummyReview(user, book);

        Notification notification = notificationRepository.save(
                new Notification(user.getId(), review.getId(), "content", "message")
        );

        Instant threshold = Instant.now().plusSeconds(1);

        // when
        int deletedCount = notificationRepository.deleteOldReadNotifications(threshold);

        em.flush();
        em.clear();

        // then
        assertThat(deletedCount).isEqualTo(0);
        assertThat(notificationRepository.findById(notification.getId()))
                .isPresent();
    }

    @Test
    @DisplayName("7일 이내 읽음 알림은 삭제되지 않아서 성공")
    void deleteOldReadNotifications_whenNotOld_thenNotDeleted() {

        // given
        User user = createDummyUser();
        Book book = createDummyBook();
        Review review = createDummyReview(user, book);

        Notification notification = notificationRepository.save(
                new Notification(user.getId(), review.getId(), "content", "message")
        );

        notification.markAsRead();
        notificationRepository.save(notification);

        Instant threshold = Instant.now().minusSeconds(1); // 과거 기준 → 삭제 안됨

        // when
        int deletedCount = notificationRepository.deleteOldReadNotifications(threshold);

        em.flush();
        em.clear();

        // then
        assertThat(deletedCount).isEqualTo(0);
        assertThat(notificationRepository.findById(notification.getId()))
                .isPresent();
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
                "isbn-123",
                null
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