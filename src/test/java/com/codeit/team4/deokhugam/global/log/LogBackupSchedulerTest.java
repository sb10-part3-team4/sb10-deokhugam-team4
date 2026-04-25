package com.codeit.team4.deokhugam.global.log;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LogBackupSchedulerTest {

    @Mock
    private LogBackupService logBackupService;

    @InjectMocks
    private LogBackupScheduler logBackupScheduler;

    @Test
    @DisplayName("매일 새벽 스케줄러가 동작할 때 로그 백업 서비스 로직 1회 호출 성공")
    void scheduleLogBackup_CallsService() {
        // when
        logBackupScheduler.scheduleLogBackup();

        // then
        verify(logBackupService, times(1)).backupAndCleanUpYesterdayLog();
    }
}