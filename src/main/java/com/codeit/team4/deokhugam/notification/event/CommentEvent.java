package com.codeit.team4.deokhugam.notification.event;

import java.util.UUID;

public record CommentEvent(
        UUID reviewId,
        UUID receiverId,
        UUID actorId
) {}