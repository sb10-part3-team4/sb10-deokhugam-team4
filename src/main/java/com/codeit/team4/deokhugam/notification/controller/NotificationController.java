package com.codeit.team4.deokhugam.notification.controller;

import com.codeit.team4.deokhugam.global.response.PageResponse;
import com.codeit.team4.deokhugam.notification.controller.api.NotificationApi;
import com.codeit.team4.deokhugam.notification.dto.NotificationResponse;
import com.codeit.team4.deokhugam.notification.service.NotificationService;
import com.codeit.team4.deokhugam.global.annotation.LoginUser;
import com.codeit.team4.deokhugam.global.dto.DeokhugamUser;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController implements NotificationApi {

    private final NotificationService notificationService;

    @Override
    @GetMapping
    public PageResponse<NotificationResponse> getNotifications(
            @LoginUser DeokhugamUser loginUser,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Instant after,
            @RequestParam(defaultValue = "10") int size
    ) {
        return notificationService.getNotifications(
                loginUser.userId(),
                cursor,
                after,
                size
        );
    }
}