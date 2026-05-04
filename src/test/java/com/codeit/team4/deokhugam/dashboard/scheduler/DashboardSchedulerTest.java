package com.codeit.team4.deokhugam.dashboard.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import com.codeit.team4.deokhugam.book.entity.Book;
import com.codeit.team4.deokhugam.book.repository.BookRepository;
import com.codeit.team4.deokhugam.config.TestContainerConfig;
import com.codeit.team4.deokhugam.dashboard.entity.PeriodType;
import com.codeit.team4.deokhugam.review.entity.Review;
import com.codeit.team4.deokhugam.review.repository.ReviewRepository;
import com.codeit.team4.deokhugam.user.entity.User;
import com.codeit.team4.deokhugam.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@SpringBootTest
@Import(TestContainerConfig.class)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class DashboardSchedulerTest {

    private static final List<String> TARGET_TYPES = List.of("BOOK", "REVIEW", "USER");

    @Autowired
    private DashboardScheduler dashboardScheduler;

    @Autowired
    private JobExplorer jobExplorer;

    @Autowired
    private Job dashboardBatchJob;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @MockitoSpyBean
    private JobLauncher jobLauncher;

    @Value("${dashboard.batch.zone}")
    private String zone;

    @BeforeEach
    void setUp() {
        User user = userRepository.saveAndFlush(new User("scheduler@test.com", "스케줄러테스터", "password123"));
        Book book = bookRepository.saveAndFlush(
                new Book("테스트 책", "저자", "설명", "출판사", LocalDate.of(2024, 1, 1), "0987654321", null)
        );
        reviewRepository.saveAndFlush(new Review(book, user, "테스트 리뷰", 5));
    }

    @Test
    @DisplayName("스케줄러 실행 시 12개 JobInstance가 모두 COMPLETED 성공")
    void runDashboardBatch_allJobInstancesCompleted_success() {
        dashboardScheduler.runDashboardBatch();

        LocalDate snapshotDate = LocalDate.now(ZoneId.of(zone)).minusDays(1);
        for (String targetType : TARGET_TYPES) {
            for (PeriodType period : PeriodType.values()) {
                JobInstance instance = jobExplorer.getJobInstance(
                        dashboardBatchJob.getName(), buildParams(snapshotDate, targetType, period));

                assertThat(instance)
                        .as("JobInstance 미생성: %s %s", targetType, period)
                        .isNotNull();

                List<JobExecution> executions = jobExplorer.getJobExecutions(instance);
                assertThat(executions)
                        .as("JobExecution 비어있음: %s %s", targetType, period)
                        .anyMatch(e -> e.getStatus() == BatchStatus.COMPLETED);
            }
        }
    }

    @Test
    @DisplayName("스케줄러 두 번 실행해도 에러 없이 완료 성공")
    void runDashboardBatch_twice_noError_success() {
        dashboardScheduler.runDashboardBatch();
        assertThatNoException().isThrownBy(() -> dashboardScheduler.runDashboardBatch());
    }

    @Test
    @DisplayName("JobLauncher가 예외를 던져도 스케줄러는 전체 중단 없이 종료 성공")
    void runDashboardBatch_jobLauncherThrows_noError_success() throws Exception {
        doThrow(new RuntimeException("강제 실패")).when(jobLauncher).run(any(), any());

        assertThatNoException().isThrownBy(() -> dashboardScheduler.runDashboardBatch());
    }

    private JobParameters buildParams(LocalDate snapshotDate, String targetType, PeriodType period) {
        return new JobParametersBuilder()
                .addLocalDate("snapshotDate", snapshotDate)
                .addString("targetType", targetType)
                .addString("period", period.name())
                .toJobParameters();
    }
}
