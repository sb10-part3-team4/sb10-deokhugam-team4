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

    @Scheduled(fixedRateString = "${user.cleanup.fixed-rate}")
    public void runCleanup() {
        userService.deleteExpiredUsers();
    }
}
