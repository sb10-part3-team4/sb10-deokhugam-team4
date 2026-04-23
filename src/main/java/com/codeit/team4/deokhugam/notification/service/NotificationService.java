package com.codeit.team4.deokhugam.notification.service;

import com.codeit.team4.deokhugam.global.error.BusinessException;
import com.codeit.team4.deokhugam.global.error.ErrorCode;
import com.codeit.team4.deokhugam.global.response.PageResponse;
import com.codeit.team4.deokhugam.notification.dto.NotificationResponse;
import com.codeit.team4.deokhugam.notification.entity.Notification;
import com.codeit.team4.deokhugam.notification.model.NotificationModel;
import com.codeit.team4.deokhugam.notification.repository.NotificationRepository;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class NotificationService {

    private final NotificationQueryService notificationQueryService;

    private final NotificationRepository notificationRepository;

    // TODO: 구현 예정
    public void createNotification(UUID receiverId, UUID reviewId, String reviewContent, String message) {
    }

    @Transactional
    public NotificationResponse markAsRead(
            UUID notificationId,
            UUID loginUserId
    ) {

        Notification notification = notificationRepository
                .findById(notificationId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.NOTIFICATION_NOT_FOUND,
                        "notificationId=" + notificationId
                ));

        if (!notification.getUserId().equals(loginUserId)) {
            throw new BusinessException(
                    ErrorCode.USER_FORBIDDEN,
                    "notificationId=" + notificationId + ", loginUserId=" + loginUserId
            );
        }

        if (!notification.isConfirmed()) {
            notification.markAsRead();
            log.info("알림 읽음 처리 완료: notificationId={}, userId={}", notificationId, loginUserId);
        }

        return toResponseFromEntity(notification);
    }

    @Transactional
    public void markAllAsRead(UUID userId) {
        List<Notification> notifications =
                notificationRepository.findByUserIdAndConfirmedFalse(userId);

        notifications.forEach(Notification::markAsRead);

        if (!notifications.isEmpty()) {
            log.info("전체 알림 읽음 처리 완료: userId={}, count={}", userId, notifications.size());
        }
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

        boolean hasNext = notifications.size() > size;

        List<NotificationModel> pageItems =
                hasNext ? notifications.subList(0, size) : notifications;

        List<NotificationResponse> content = pageItems.stream()
                .map(this::toResponseFromEntity)
                .toList();

        String nextCursor = null;
        Instant nextAfter = null;

        if (hasNext) {
            NotificationModel last = pageItems.get(pageItems.size() - 1);
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

    @Transactional
    public void deleteExpiredNotifications() {

        Instant threshold = Instant.now().minus(7, ChronoUnit.DAYS);

        int deletedCount = notificationRepository.deleteOldReadNotifications(threshold);

        log.info("알림 물리 삭제 완료: count={}, threshold={}", deletedCount, threshold);
    }

    // 헬퍼 메서드
    // JOOQ 조회 결과 → DTO
    private NotificationResponse toResponseFromEntity(NotificationModel n) {
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

    // JPA 엔티티 → DTO
    private NotificationResponse toResponseFromEntity(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getUserId(),
                n.getReviewId(),
                n.getReviewContent(),
                n.getMessage(),
                n.isConfirmed(),
                n.getCreatedAt(),
                n.getUpdatedAt()
        );
    }
}