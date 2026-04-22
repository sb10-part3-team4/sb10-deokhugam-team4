package com.codeit.team4.deokhugam.notification.controller;

import com.codeit.team4.deokhugam.notification.controller.api.NotificationApi;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController implements NotificationApi {

}