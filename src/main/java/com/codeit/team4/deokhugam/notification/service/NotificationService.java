package com.codeit.team4.deokhugam.notification.service;

import com.codeit.team4.deokhugam.dashboard.entity.PeriodType;
import com.codeit.team4.deokhugam.global.error.BusinessException;
import com.codeit.team4.deokhugam.global.error.ErrorCode;
import com.codeit.team4.deokhugam.global.response.PageResponse;
import com.codeit.team4.deokhugam.notification.dto.NotificationResponse;
import com.codeit.team4.deokhugam.notification.entity.Notification;
import com.codeit.team4.deokhugam.notification.model.NotificationModel;
import com.codeit.team4.deokhugam.notification.repository.NotificationRepository;
import com.codeit.team4.deokhugam.review.entity.Review;
import com.codeit.team4.deokhugam.review.repository.ReviewRepository;
import com.codeit.team4.deokhugam.user.service.UserService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class NotificationService {

    private final NotificationQueryService notificationQueryService;
    private final NotificationRepository notificationRepository;
    private final ReviewRepository reviewRepository;
    private final UserService userService;

    @Transactional
    public void createLikeNotification(UUID receiverId, UUID reviewId, UUID actorId) {

        String actorName = getActorNickname(actorId);
        Review review = findActiveReview(reviewId);

        saveNotification(
                receiverId,
                reviewId,
                review,
                "[" + actorName + "]님이 나의 리뷰를 좋아합니다."
        );
    }

    @Transactional
    public void createCommentNotification(UUID receiverId, UUID reviewId, UUID actorId) {

        String actorName = getActorNickname(actorId);
        Review review = findActiveReview(reviewId);

        saveNotification(
                receiverId,
                reviewId,
                review,
                "[" + actorName + "]님이 나의 리뷰에 댓글을 남겼습니다."
        );
    }

    @Transactional
    public void createRankNotification(UUID receiverId, UUID reviewId, PeriodType period, int rank) {
        String periodText = switch (period) {
            case DAILY -> "일간";
            case WEEKLY -> "주간";
            case MONTHLY -> "월간";
            case ALL_TIME -> "전체";
        };

        Review review = findActiveReview(reviewId);

        saveNotification(
                receiverId,
                reviewId,
                review,
                "나의 리뷰가 " + periodText + " TOP10에 진입했습니다. (" + rank + "위)"
        );
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

    private String getActorNickname(UUID actorId) {
        return userService.findById(actorId).getNickname();
    }

    private Review findActiveReview(UUID reviewId) {
        return reviewRepository.findByIdAndDeletedAtIsNull(reviewId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.REVIEW_NOT_FOUND,
                        "reviewId=" + reviewId
                ));
    }

    private void saveNotification(UUID receiverId, UUID reviewId, Review review, String message) {
        Notification notification = new Notification(
                receiverId, reviewId, review.getContent(), message
        );

        notificationRepository.saveAndFlush(notification);
    }
}