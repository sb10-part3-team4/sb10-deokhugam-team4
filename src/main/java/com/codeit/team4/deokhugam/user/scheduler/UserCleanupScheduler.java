package com.codeit.team4.deokhugam.user.scheduler;

import com.codeit.team4.deokhugam.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserCleanupScheduler {

    private final UserService userService;

    @Scheduled(cron = "${user.cleanup.cron}")
    public void runCleanup() {
        log.info("유저 물리 삭제 스케줄러 시작");
        try {
            userService.deleteExpiredSoftDeletedUsers();
            log.info("유저 물리 삭제 스케줄러 완료");
        } catch (Exception e) {
            log.error("유저 물리 삭제 스케줄러 실패", e);
        }
    }
}
