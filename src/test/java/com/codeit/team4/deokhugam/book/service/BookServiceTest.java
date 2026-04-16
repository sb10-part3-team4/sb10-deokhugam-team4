package com.codeit.team4.deokhugam.book.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeit.team4.deokhugam.book.dto.BookCreateRequest;
import com.codeit.team4.deokhugam.book.dto.BookDto;
import com.codeit.team4.deokhugam.book.entity.Book;
import com.codeit.team4.deokhugam.book.mapper.BookMapper;
import com.codeit.team4.deokhugam.book.repository.BookRepository;
import com.codeit.team4.deokhugam.global.error.BusinessException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    BookRepository bookRepository;

    @Mock
    BookMapper bookMapper;

    @InjectMocks
    BookService bookService;

    @Test
    @DisplayName("도서 등록 성공")
    void create_book_success(){
        // given
        BookCreateRequest request = new BookCreateRequest("달선이의 하루", "달선", "달선이의 하루를 담은 책입니다.", "달출판사", LocalDate.of(2026, 1, 1),
                "978-89-91995-00-1");
        BookDto bookDto = new BookDto(UUID.randomUUID(), request.title(), request.author(),
                request.description(), request.publisher(), request.publishedDate(), request.isbn(),
                null, 0, BigDecimal.ZERO, Instant.now(), Instant.now());

        when(bookRepository.existsByIsbnAndDeletedAtIsNull(anyString())).thenReturn(false);
        when(bookMapper.toBookDto(any(Book.class))).thenReturn(bookDto);

        // when
        BookDto book = bookService.createBook(request);

        // then
        assertThat(book.author()).isEqualTo("달선");
    }

    @Test
    @DisplayName("도서 등록 실패 - isbn 중복")
    void create_book_fail_duplicate_isbn(){
        // given
        BookCreateRequest request = new BookCreateRequest("달선이의 하루", "달선", "달선이의 하루를 담은 책입니다.", "달출판사", LocalDate.of(2026, 1, 1),
                "978-89-91995-00-1");

        when(bookRepository.existsByIsbnAndDeletedAtIsNull(request.isbn())).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> bookService.createBook(request))
                .isInstanceOf(BusinessException.class);

        verify(bookRepository, never()).save(any(Book.class));
        verify(bookMapper, never()).toBookDto(any(Book.class));

    }
}