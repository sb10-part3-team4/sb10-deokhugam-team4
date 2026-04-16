package com.codeit.team4.deokhugam.book.dto;

import java.time.Instant;
import java.util.List;

public record CursorPageResponseBookDto(
        List<BookDto> content,
        Object nextCursor,
        Instant nextAfter,
        int size,
        int totalElements,
        boolean hasNext
) {

}
