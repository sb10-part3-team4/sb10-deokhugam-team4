package com.codeit.team4.deokhugam.dashboard.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeit.team4.deokhugam.book.entity.Book;
import com.codeit.team4.deokhugam.book.repository.BookRepository;
import com.codeit.team4.deokhugam.config.TestContainerConfig;
import com.codeit.team4.deokhugam.dashboard.repository.PopularBookRepository;
import com.codeit.team4.deokhugam.dashboard.repository.PopularReviewRepository;
import com.codeit.team4.deokhugam.dashboard.repository.PowerUserRepository;
import com.codeit.team4.deokhugam.review.entity.Review;
import com.codeit.team4.deokhugam.review.repository.ReviewRepository;
import com.codeit.team4.deokhugam.user.entity.User;
import com.codeit.team4.deokhugam.user.repository.UserRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Import(TestContainerConfig.class)
@ActiveProfiles("test")
class DashboardBatchConfigTest {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job popularBooksJob;

    @Autowired
    private Job popularReviewsJob;

    @Autowired
    private Job powerUsersJob;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private PopularBookRepository popularBookRepository;

    @Autowired
    private PopularReviewRepository popularReviewRepository;

    @Autowired
    private PowerUserRepository powerUserRepository;

    private LocalDate snapshotDate;

    @BeforeEach
    void setUp() {
        snapshotDate = LocalDate.now();
        User user = userRepository.saveAndFlush(new User("batch@test.com", "배치테스터", "password123"));
        Book book = bookRepository.saveAndFlush(
                new Book("배치 테스트 책", "저자", "설명", "출판사", LocalDate.of(2024, 1, 1), "1234567890", null)
        );
        reviewRepository.saveAndFlush(new Review(book, user, "배치 테스트 리뷰", 5));
    }

    @AfterEach
    void tearDown() {
        popularBookRepository.deleteAll();
        popularReviewRepository.deleteAll();
        powerUserRepository.deleteAll();
        reviewRepository.deleteAll();
        bookRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("인기 도서 Job 실행 성공")
    void popularBooksJob_success() throws Exception {
        // when
        JobExecution execution = jobLauncher.run(popularBooksJob, new JobParametersBuilder()
                .addLocalDate("snapshotDate", snapshotDate)
                .toJobParameters());

        // then
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(popularBookRepository.findAll()).isNotEmpty();
    }

    @Test
    @DisplayName("인기 리뷰 Job 실행 성공")
    void popularReviewsJob_success() throws Exception {
        // when
        JobExecution execution = jobLauncher.run(popularReviewsJob, new JobParametersBuilder()
                .addLocalDate("snapshotDate", snapshotDate)
                .toJobParameters());

        // then
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(popularReviewRepository.findAll()).isNotEmpty();
    }

    @Test
    @DisplayName("파워 유저 Job 실행 성공")
    void powerUsersJob_success() throws Exception {
        // when
        JobExecution execution = jobLauncher.run(powerUsersJob, new JobParametersBuilder()
                .addLocalDate("snapshotDate", snapshotDate)
                .toJobParameters());

        // then
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(powerUserRepository.findAll()).isNotEmpty();
    }

}
