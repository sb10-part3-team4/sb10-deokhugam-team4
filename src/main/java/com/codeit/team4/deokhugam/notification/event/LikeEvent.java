package com.codeit.team4.deokhugam.notification.event;

import java.util.UUID;

public record LikeEvent(
        UUID reviewId,
        UUID receiverId,
        UUID actorId
) {}