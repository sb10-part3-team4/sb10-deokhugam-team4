package com.codeit.team4.deokhugam.notification.service;

import com.codeit.team4.deokhugam.book.entity.Book;
import com.codeit.team4.deokhugam.book.repository.BookRepository;
import com.codeit.team4.deokhugam.config.TestContainerConfig;
import com.codeit.team4.deokhugam.notification.entity.Notification;
import com.codeit.team4.deokhugam.notification.model.NotificationModel;
import com.codeit.team4.deokhugam.notification.repository.NotificationRepository;
import com.codeit.team4.deokhugam.review.entity.Review;
import com.codeit.team4.deokhugam.review.repository.ReviewRepository;
import com.codeit.team4.deokhugam.user.entity.User;
import com.codeit.team4.deokhugam.user.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestContainerConfig.class)
@Transactional
class NotificationQueryServiceTest {

    @Autowired
    NotificationQueryService notificationQueryService;

    @Autowired
    NotificationRepository notificationRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ReviewRepository reviewRepository;

    @Autowired
    BookRepository bookRepository;

    @Test
    @DisplayName("알림 최신순 정렬 목록 조회 성공")
    void findNotifications_orderByCreatedAtDesc_success() throws Exception {

        // given
        User user = createDummyUser();
        Book book = createDummyBook();
        Review review = createDummyReview(user, book);

        Notification n1 = new Notification(
                user.getId(),
                review.getId(),
                "content1",
                "msg1"
        );

        notificationRepository.save(n1);
        notificationRepository.flush();

        Thread.sleep(5);

        Notification n2 = new Notification(
                user.getId(),
                review.getId(),
                "content2",
                "msg2"
        );

        notificationRepository.save(n2);
        notificationRepository.flush();

        // when
        List<NotificationModel> result =
                notificationQueryService.findNotifications(user.getId(), null, 10);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).createdAt())
                .isAfterOrEqualTo(result.get(1).createdAt());
    }

    @Test
    @DisplayName("알림 cursor 기준 이전 데이터만 조회 목록 조회 성공")
    void findNotifications_cursorBefore_success() throws InterruptedException {

        // given
        User user = createDummyUser();
        Book book = createDummyBook();
        Review review = createDummyReview(user, book);

        Notification old = new Notification(
                user.getId(),
                review.getId(),
                "old",
                "msg"
        );

        Notification newN = new Notification(
                user.getId(),
                review.getId(),
                "new",
                "msg"
        );

        notificationRepository.save(old);
        notificationRepository.flush();

        Thread.sleep(1);

        notificationRepository.save(newN);
        notificationRepository.flush();

        // when
        List<NotificationModel> result =
                notificationQueryService.findNotifications(
                        user.getId(),
                        newN.getCreatedAt(),
                        10
                );

        // then
        assertThat(result).isNotEmpty();
        assertThat(result)
                .allMatch(n -> n.createdAt().isBefore(newN.getCreatedAt()));
    }

    @Test
    @DisplayName("다른 사용자의 알림은 조회되지 않는 목록 조회 성공")
    void findNotifications_onlyUserNotifications_success() {

        // given
        User user = createDummyUser();
        User other = createDummyUser();

        Book book = createDummyBook();
        Review review = createDummyReview(user, book);

        notificationRepository.save(
                new Notification(
                        other.getId(),
                        review.getId(),
                        "x",
                        "m"
                )
        );

        notificationRepository.save(
                new Notification(
                        user.getId(),
                        review.getId(),
                        "y",
                        "m"
                )
        );

        notificationRepository.flush();

        // when
        List<NotificationModel> result =
                notificationQueryService.findNotifications(user.getId(), null, 10);

        // then
        assertThat(result)
                .isNotEmpty()
                .allMatch(n -> n.userId().equals(user.getId()));
    }

    @Test
    @DisplayName("알림 목록 조회 시 limit 개수만큼만 조회 성공")
    void findNotifications_limitApplied_success() {

        // given
        User user = createDummyUser();
        Book book = createDummyBook();
        Review review = createDummyReview(user, book);

        for (int i = 0; i < 5; i++) {
            notificationRepository.save(
                    new Notification(
                            user.getId(),
                            review.getId(),
                            "c" + i,
                            "m"
                    )
            );
        }

        notificationRepository.flush();

        // when
        List<NotificationModel> result =
                notificationQueryService.findNotifications(user.getId(), null, 3);

        // then
        assertThat(result).hasSizeLessThanOrEqualTo(4);
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
                "isbn-123"
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