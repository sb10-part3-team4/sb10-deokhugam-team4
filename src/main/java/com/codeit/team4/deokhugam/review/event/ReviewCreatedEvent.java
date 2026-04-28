package com.codeit.team4.deokhugam.review.event;

import java.util.UUID;

public record ReviewCreatedEvent(
        UUID bookId,
        int rating
) {

}
