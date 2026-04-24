package com.codeit.team4.deokhugam.naver;

import java.util.List;

public record NaverBookResponse(
        List<NaverBookItem> items
) {
    public record NaverBookItem(
            String title,
            String author,
            String publisher,
            String pubdate,
            String isbn,
            String description,
            String image
    ) {}
}