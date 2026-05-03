package com.codeit.team4.deokhugam.dashboard.controller;

import com.codeit.team4.deokhugam.dashboard.controller.api.DashboardAdminApi;
import com.codeit.team4.deokhugam.dashboard.service.DashboardBatchService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
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

    private final JobLauncher jobLauncher;
    private final Job popularBooksJob;
    private final Job popularReviewsJob;
    private final Job powerUsersJob;

    @Value("${dashboard.batch.zone}")
    private String zone;

    @PostMapping("/batch")
    public ResponseEntity<Void> runBatch() {
        LocalDate snapshotDate = DashboardBatchService.defaultSnapshotDate(zone);
        log.info("수동 배치 실행 요청: snapshotDate={}", snapshotDate);

        try {
            var params = new JobParametersBuilder()
                    .addLocalDate("snapshotDate", snapshotDate)
                    .toJobParameters();

            jobLauncher.run(popularBooksJob, params);
            jobLauncher.run(popularReviewsJob, params);
            jobLauncher.run(powerUsersJob, params);
        } catch (Exception e) {
            log.error("수동 배치 실행 실패", e);
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.noContent().build();
    }
}
