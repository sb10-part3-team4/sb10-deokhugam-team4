package com.codeit.team4.deokhugam.book.mapper;

import com.codeit.team4.deokhugam.book.dto.BookResponse;
import com.codeit.team4.deokhugam.book.dto.NaverBookSearchResponse;
import com.codeit.team4.deokhugam.book.entity.Book;
import com.codeit.team4.deokhugam.naver.NaverBookResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.math.BigDecimal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookMapper {

    BookResponse toResponse(Book book, int reviewCount, BigDecimal rating);

    default BookResponse toResponse(Book book) {
        return toResponse(book, 0, BigDecimal.ZERO);
    }

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
