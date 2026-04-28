package com.codeit.team4.deokhugam.review.event;

import java.util.UUID;

public record ReviewDeletedEvent(
        UUID bookId,
        int rating
) {

}
