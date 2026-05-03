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
    public Job dashboardBatchJob() {
        return new JobBuilder("dashboardBatchJob", jobRepository)
                .start(dashboardBatchStep())
                .build();
    }

    @Bean
    public Step dashboardBatchStep() {
        return new StepBuilder("dashboardBatchStep", jobRepository)
                .tasklet(dashboardBatchTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Tasklet dashboardBatchTasklet() {
        return (contribution, chunkContext) -> {
            LocalDate snapshotDate = extractParam(chunkContext, "snapshotDate", LocalDate.class);
            String targetType = extractParam(chunkContext, "targetType", String.class);
            PeriodType period = PeriodType.valueOf(extractParam(chunkContext, "period", String.class));

            switch (targetType) {
                case "BOOK" -> dashboardBatchService.updatePopularBooksByPeriod(period, snapshotDate);
                case "REVIEW" -> dashboardBatchService.updatePopularReviewsByPeriod(period, snapshotDate);
                case "USER" -> dashboardBatchService.updatePowerUsersByPeriod(period, snapshotDate);
                default -> throw new IllegalArgumentException("Unknown targetType: " + targetType);
            }

            return RepeatStatus.FINISHED;
        };
    }

    @SuppressWarnings("unchecked")
    private <T> T extractParam(ChunkContext chunkContext, String key, Class<T> type) {
        return (T) chunkContext.getStepContext()
                .getJobParameters()
                .get(key);
    }
}
