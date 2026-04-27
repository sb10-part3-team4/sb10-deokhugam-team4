package com.codeit.team4.deokhugam.review.event;

import java.util.UUID;

public record ReviewUpdatedEvent(
        UUID bookId,
        int oldRating,
        int newRating
) {

}
