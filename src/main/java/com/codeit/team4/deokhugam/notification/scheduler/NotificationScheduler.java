package com.codeit.team4.deokhugam.notification.scheduler;

import com.codeit.team4.deokhugam.notification.service.NotificationService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final NotificationService notificationService;

    @Value("${notification.batch.zone}")
    private String zone;

    @Scheduled(cron = "${notification.batch.cron}", zone = "${notification.batch.zone}")
    public void runNotificationBatch() {
        Instant now = Instant.now();
        log.info("알림 삭제 스케줄러 시작: now={}, zone={}", now, zone);
        try {
            notificationService.deleteExpiredNotifications();
            log.info("알림 삭제 스케줄러 완료");
        } catch (Exception e) {
            log.error("알림 삭제 스케줄러 실패", e);
        }
    }
}