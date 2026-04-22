package com.codeit.team4.deokhugam.notification.service;

import com.codeit.team4.deokhugam.global.response.PageResponse;
import com.codeit.team4.deokhugam.notification.dto.NotificationResponse;
import com.codeit.team4.deokhugam.notification.dto.NotificationUpdateRequest;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    // TODO: 구현 예정
    public void createNotification(UUID receiverId, UUID reviewId, String reviewContent, String message) {
    }

    // TODO: 구현 예정
    @Transactional
    public NotificationResponse markAsRead(UUID notificationId, UUID loginUserId, NotificationUpdateRequest request) {
        return null;
    }

    // TODO: 구현 예정
    @Transactional
    public void markAllAsRead(UUID loginUserId) {
    }

    // TODO: 구현 예정
    public PageResponse<NotificationResponse> getNotifications(
            UUID loginUserId,
            String cursor,
            Instant after,
            int size
    ) {
        return null;
    }

    // TODO: 구현 예정
    @Transactional
    public void deleteExpiredNotifications() {
    }
}