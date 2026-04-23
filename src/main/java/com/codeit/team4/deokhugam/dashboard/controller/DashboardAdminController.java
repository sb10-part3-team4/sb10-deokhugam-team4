package com.codeit.team4.deokhugam.dashboard.controller;

import com.codeit.team4.deokhugam.dashboard.service.DashboardBatchService;
import java.time.LocalDate;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardAdminController {

    private final DashboardBatchService dashboardBatchService;

    @Value("${dashboard.batch.zone}")
    private String zone;

    @PostMapping("/batch")
    public ResponseEntity<String> runBatch() {
        LocalDate snapshotDate = LocalDate.now(ZoneId.of(zone));
        log.info("수동 배치 실행 요청: snapshotDate={}", snapshotDate);
        dashboardBatchService.updatePopularBooks(snapshotDate);
        return ResponseEntity.ok("배치 실행 완료: snapshotDate=" + snapshotDate);
    }
}
