package com.codeit.team4.deokhugam.dashboard.controller;

import com.codeit.team4.deokhugam.dashboard.controller.api.DashboardAdminApi;
import com.codeit.team4.deokhugam.dashboard.entity.PeriodType;
import com.codeit.team4.deokhugam.dashboard.service.DashboardBatchService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardAdminController implements DashboardAdminApi {

    private static final List<String> TARGET_TYPES = List.of("BOOK", "REVIEW", "USER");

    private final JobLauncher jobLauncher;
    private final Job dashboardBatchJob;

    @Value("${dashboard.batch.zone}")
    private String zone;

    @PostMapping("/batch")
    public ResponseEntity<Void> runBatch() {
        LocalDate snapshotDate = DashboardBatchService.defaultSnapshotDate(zone);
        log.info("수동 배치 실행 요청: snapshotDate={}", snapshotDate);

        int failureCount = 0;
        for (String targetType : TARGET_TYPES) {
            for (PeriodType period : PeriodType.values()) {
                JobParameters params = new JobParametersBuilder()
                        .addLocalDate("snapshotDate", snapshotDate)
                        .addString("targetType", targetType)
                        .addString("period", period.name())
                        .toJobParameters();
                try {
                    JobExecution execution = jobLauncher.run(dashboardBatchJob, params);
                    if (execution.getStatus() != BatchStatus.COMPLETED) {
                        failureCount++;
                        log.error("수동 배치 비정상 종료: {} {} status={}",
                                targetType, period, execution.getStatus());
                    }
                } catch (Exception e) {
                    failureCount++;
                    log.error("수동 배치 실행 실패: {} {}", targetType, period, e);
                }
            }
        }

        if (failureCount > 0) {
            log.error("수동 배치 일부 실패: failureCount={}", failureCount);
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.noContent().build();
    }
}
