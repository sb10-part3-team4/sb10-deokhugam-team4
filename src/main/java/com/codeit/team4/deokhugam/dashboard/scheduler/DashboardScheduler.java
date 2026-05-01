package com.codeit.team4.deokhugam.dashboard.scheduler;

import com.codeit.team4.deokhugam.dashboard.service.DashboardBatchService;
import java.time.LocalDate;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DashboardScheduler {

    private final DashboardBatchService dashboardBatchService;

    @Value("${dashboard.batch.zone}")
    private String zone;

    //TODO: Lock 추가 후 개선 예정
    @Scheduled(cron = "${dashboard.batch.cron}", zone = "${dashboard.batch.zone}")
    public void runDashboardBatch() {
        LocalDate snapshotDate = LocalDate.now(ZoneId.of(zone)).minusDays(1);
        log.info("대시보드 배치 스케줄러 시작: snapshotDate={}", snapshotDate);

        try {
            dashboardBatchService.updatePopularBooks(snapshotDate);
        } catch (Exception e) {
            log.error("인기 도서 배치 실패", e);
        }

        try {
            dashboardBatchService.updatePopularReviews(snapshotDate);
        } catch (Exception e) {
            log.error("인기 리뷰 배치 실패", e);
        }

        try {
            dashboardBatchService.updatePowerUsers(snapshotDate);
        } catch (Exception e) {
            log.error("파워 유저 배치 실패", e);
        }

        log.info("대시보드 배치 스케줄러 종료");
    }
}
