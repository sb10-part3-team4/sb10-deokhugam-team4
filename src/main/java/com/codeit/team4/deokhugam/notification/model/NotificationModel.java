package com.codeit.team4.deokhugam.notification.model;

import java.time.Instant;
import java.util.UUID;

public record NotificationModel(
        UUID id,
        UUID userId,
        UUID reviewId,
        String reviewContent,
        String message,
        boolean confirmed,
        Instant createdAt,
        Instant updatedAt
) {}
