package com.codeit.team4.deokhugam.book.mapper;

import com.codeit.team4.deokhugam.book.dto.BookResponse;
import com.codeit.team4.deokhugam.book.dto.NaverBookSearchResponse;
import com.codeit.team4.deokhugam.book.entity.Book;
import com.codeit.team4.deokhugam.naver.NaverBookResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookMapper {

    BookResponse toResponse(Book book);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publishedDate", expression = "java(parsePubdate(item.pubdate()))")
    @Mapping(target = "thumbnailUrl", source = "thumbnailBase64")
    @Mapping(target = "reviewCount", constant = "0")
    @Mapping(target = "rating", expression = "java(java.math.BigDecimal.ZERO)")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    BookResponse toBookResponse(NaverBookResponse.NaverBookItem item, String thumbnailBase64);

    @Mapping(target = "publishedDate", expression = "java(parsePubdate(item.pubdate()))")
    @Mapping(source = "item.isbn", target = "isbn")
    @Mapping(source = "thumbnailBase64", target = "thumbnailImage")
    NaverBookSearchResponse toNaverBookSearchResponse(NaverBookResponse.NaverBookItem item, String thumbnailBase64);

    default LocalDate parsePubdate(String pubdate) {
        if (pubdate == null || pubdate.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(pubdate, DateTimeFormatter.ofPattern("yyyyMMdd"));
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}