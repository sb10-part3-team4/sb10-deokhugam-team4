package com.codeit.team4.deokhugam.notification.entity;

import com.codeit.team4.deokhugam.global.entity.BaseUpdatableEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "notifications",
        indexes = {
                @Index(name = "idx_notifications_user_created", columnList = "user_id, created_at DESC"),
                @Index(name = "idx_notifications_confirmed_at", columnList = "confirmed_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseUpdatableEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "review_id", nullable = false)
    private UUID reviewId;

    @Column(name = "review_content", nullable = false)
    private String reviewContent;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private boolean confirmed;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    public Notification(UUID userId, UUID reviewId, String reviewContent, String message) {
        this.userId = userId;
        this.reviewId = reviewId;
        this.reviewContent = reviewContent;
        this.message = message;
        this.confirmed = false;
    }

    public void markAsRead() {
        if (!this.confirmed) {
            this.confirmed = true;
            this.confirmedAt = Instant.now();
        }
    }

    public boolean isOwner(UUID userId) {
        return this.userId.equals(userId);
    }
}