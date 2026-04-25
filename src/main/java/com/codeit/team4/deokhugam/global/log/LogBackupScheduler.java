package com.codeit.team4.deokhugam.global.log;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogBackupScheduler {

    private final LogBackupService logBackupService;

    @Scheduled(cron = "${log.backup.cron}")
    public void scheduleLogBackup() {
        log.info("로그 파일 백업 스케줄러 실행");

        logBackupService.backupAndCleanUpYesterdayLog();

        log.info("로그 파일 백업 스케줄러 실행 종료");
    }
}