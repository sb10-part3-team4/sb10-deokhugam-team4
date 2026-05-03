package com.codeit.team4.deokhugam.dashboard.scheduler;

import com.codeit.team4.deokhugam.dashboard.service.DashboardBatchService;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DashboardScheduler {

    private final JobLauncher jobLauncher;
    private final JobExplorer jobExplorer;
    private final Job popularBooksJob;
    private final Job popularReviewsJob;
    private final Job powerUsersJob;

    @Value("${dashboard.batch.zone}")
    private String zone;

    @Scheduled(cron = "${dashboard.batch.cron}", zone = "${dashboard.batch.zone}")
    public void runDashboardBatch() {
        LocalDate today = DashboardBatchService.defaultSnapshotDate(zone);
        LocalDate startDate = findStartDate(today);

        log.info("대시보드 배치 스케줄러 시작: startDate={}, today={}", startDate, today);

        for (LocalDate date = startDate; !date.isAfter(today); date = date.plusDays(1)) {
            runAllJobsForDate(date);
        }

        log.info("대시보드 배치 스케줄러 종료");
    }

    private void runAllJobsForDate(LocalDate snapshotDate) {
        JobParameters params = new JobParametersBuilder()
                .addLocalDate("snapshotDate", snapshotDate)
                .toJobParameters();

        CompletableFuture.allOf(
                CompletableFuture.runAsync(() -> runJob(popularBooksJob, params)),
                CompletableFuture.runAsync(() -> runJob(popularReviewsJob, params)),
                CompletableFuture.runAsync(() -> runJob(powerUsersJob, params))
        ).join();
    }

    private void runJob(Job job, JobParameters params) {
        try {
            jobLauncher.run(job, params);
        } catch (JobInstanceAlreadyCompleteException e) {
            log.debug("{} 이미 완료됨, 스킵", job.getName());
        } catch (Exception e) {
            log.error("{} 실행 실패", job.getName(), e);
        }
    }

    private LocalDate findStartDate(LocalDate today) {
        LocalDate candidate = today;

        for (LocalDate date = today.minusDays(1); !date.isBefore(today.minusDays(30)); date = date.minusDays(1)) {
            if (allJobsCompletedForDate(date)) {
                candidate = date.plusDays(1);
                break;
            }
        }

        if (candidate.isAfter(today)) {
            return today;
        }
        return candidate;
    }

    private boolean allJobsCompletedForDate(LocalDate date) {
        JobParameters params = new JobParametersBuilder()
                .addLocalDate("snapshotDate", date)
                .toJobParameters();

        return isJobCompleted(popularBooksJob, params)
                && isJobCompleted(popularReviewsJob, params)
                && isJobCompleted(powerUsersJob, params);
    }

    private boolean isJobCompleted(Job job, JobParameters params) {
        var jobInstance = jobExplorer.getJobInstance(job.getName(), params);
        if (jobInstance == null) {
            return false;
        }

        List<JobExecution> executions = jobExplorer.getJobExecutions(jobInstance);
        if (executions == null || executions.isEmpty()) {
            return false;
        }

        return executions.stream()
                .anyMatch(e -> e.getStatus() == BatchStatus.COMPLETED);
    }
}
