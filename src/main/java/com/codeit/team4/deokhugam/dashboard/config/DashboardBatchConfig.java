package com.codeit.team4.deokhugam.dashboard.config;

import com.codeit.team4.deokhugam.dashboard.entity.PeriodType;
import com.codeit.team4.deokhugam.dashboard.service.DashboardBatchService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DashboardBatchConfig {

    private final DashboardBatchService dashboardBatchService;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job popularBooksJob() {
        return new JobBuilder("popularBooksJob", jobRepository)
                .start(popularBooksStep())
                .build();
    }

    @Bean
    public Job popularReviewsJob() {
        return new JobBuilder("popularReviewsJob", jobRepository)
                .start(popularReviewsStep())
                .build();
    }

    @Bean
    public Job powerUsersJob() {
        return new JobBuilder("powerUsersJob", jobRepository)
                .start(powerUsersStep())
                .build();
    }

    @Bean
    public Step popularBooksStep() {
        return new StepBuilder("popularBooksStep", jobRepository)
                .tasklet(popularBooksTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Step popularReviewsStep() {
        return new StepBuilder("popularReviewsStep", jobRepository)
                .tasklet(popularReviewsTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Step powerUsersStep() {
        return new StepBuilder("powerUsersStep", jobRepository)
                .tasklet(powerUsersTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Tasklet popularBooksTasklet() {
        return (contribution, chunkContext) -> {
            LocalDate snapshotDate = extractSnapshotDate(chunkContext);
            for (PeriodType period : PeriodType.values()) {
                dashboardBatchService.updatePopularBooksByPeriod(period, snapshotDate);
            }
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public Tasklet popularReviewsTasklet() {
        return (contribution, chunkContext) -> {
            LocalDate snapshotDate = extractSnapshotDate(chunkContext);
            for (PeriodType period : PeriodType.values()) {
                dashboardBatchService.updatePopularReviewsByPeriod(period, snapshotDate);
            }
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public Tasklet powerUsersTasklet() {
        return (contribution, chunkContext) -> {
            LocalDate snapshotDate = extractSnapshotDate(chunkContext);
            for (PeriodType period : PeriodType.values()) {
                dashboardBatchService.updatePowerUsersByPeriod(period, snapshotDate);
            }
            return RepeatStatus.FINISHED;
        };
    }

    private LocalDate extractSnapshotDate(ChunkContext chunkContext) {
        return (LocalDate) chunkContext.getStepContext()
                .getJobParameters()
                .get("snapshotDate");
    }
}
