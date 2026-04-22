package com.codeit.team4.deokhugam.notification.service;

import com.codeit.team4.deokhugam.global.response.PageResponse;
import com.codeit.team4.deokhugam.notification.dto.NotificationResponse;
import com.codeit.team4.deokhugam.notification.dto.NotificationUpdateRequest;
import com.codeit.team4.deokhugam.notification.model.NotificationModel;
import com.codeit.team4.deokhugam.notification.repository.NotificationRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;

    private final NotificationQueryService notificationQueryService;

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

    public PageResponse<NotificationResponse> getNotifications(
            UUID loginUserId,
            String cursor,
            Instant after,
            int size
    ) {
        Instant cursorTime = after;

        List<NotificationModel> notifications =
                notificationQueryService.findNotifications(
                        loginUserId,
                        cursorTime,
                        size
                );

        List<NotificationResponse> content = notifications.stream()
                .map(this::toResponse)
                .toList();
        boolean hasNext = notifications.size() == size;

        String nextCursor = null;
        Instant nextAfter = null;

        if (!notifications.isEmpty() && hasNext) {
            NotificationModel last = notifications.get(notifications.size() - 1);
            nextCursor = last.id().toString();
            nextAfter = last.createdAt();
        }

        return new PageResponse<>(
                content,
                nextCursor,
                nextAfter,
                size,
                null,
                hasNext
        );
    }

    // TODO: 구현 예정
    @Transactional
    public void deleteExpiredNotifications() {
    }

    // 헬퍼 메서드
    private NotificationResponse toResponse(NotificationModel n) {
        return new NotificationResponse(
                n.id(),
                n.userId(),
                n.reviewId(),
                n.reviewContent(),
                n.message(),
                n.confirmed(),
                n.createdAt(),
                n.updatedAt()
        );
    }
}