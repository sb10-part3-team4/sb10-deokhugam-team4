package com.codeit.team4.deokhugam.dashboard.scheduler;

import static org.assertj.core.api.Assertions.assertThatNoException;

import com.codeit.team4.deokhugam.book.entity.Book;
import com.codeit.team4.deokhugam.book.repository.BookRepository;
import com.codeit.team4.deokhugam.config.TestContainerConfig;
import com.codeit.team4.deokhugam.review.entity.Review;
import com.codeit.team4.deokhugam.review.repository.ReviewRepository;
import com.codeit.team4.deokhugam.user.entity.User;
import com.codeit.team4.deokhugam.user.repository.UserRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Import(TestContainerConfig.class)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class DashboardSchedulerTest {

    @Autowired
    private DashboardScheduler dashboardScheduler;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @BeforeEach
    void setUp() {
        User user = userRepository.saveAndFlush(new User("scheduler@test.com", "스케줄러테스터", "password123"));
        Book book = bookRepository.saveAndFlush(
                new Book("테스트 책", "저자", "설명", "출판사", LocalDate.of(2024, 1, 1), "0987654321", null)
        );
        reviewRepository.saveAndFlush(new Review(book, user, "테스트 리뷰", 5));
    }

    @Test
    @DisplayName("스케줄러 실행 시 에러 없이 완료 성공")
    void runDashboardBatch_noError() {
        assertThatNoException().isThrownBy(() -> dashboardScheduler.runDashboardBatch());
    }

    @Test
    @DisplayName("스케줄러 두 번 실행해도 에러 없이 완료 성공")
    void runDashboardBatch_twice_noError() {
        dashboardScheduler.runDashboardBatch();
        assertThatNoException().isThrownBy(() -> dashboardScheduler.runDashboardBatch());
    }

}
