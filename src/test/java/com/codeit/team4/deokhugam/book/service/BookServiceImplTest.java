package com.codeit.team4.deokhugam.book.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeit.team4.deokhugam.book.dto.BookCreateRequest;
import com.codeit.team4.deokhugam.book.dto.BookResponse;
import com.codeit.team4.deokhugam.book.entity.Book;
import com.codeit.team4.deokhugam.book.mapper.BookMapper;
import com.codeit.team4.deokhugam.book.repository.BookRepository;
import com.codeit.team4.deokhugam.global.error.BusinessException;
import com.codeit.team4.deokhugam.global.error.ErrorCode;
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
class BookServiceImplTest {

    @Mock
    BookRepository bookRepository;

    @Mock
    BookMapper bookMapper;

    @InjectMocks
    BookServiceImpl bookServiceImpl;

    @Test
    @DisplayName("도서 등록 성공")
    void create_book_success(){
        // given
        BookCreateRequest request = new BookCreateRequest("달선이의 하루", "달선", "달선이의 하루를 담은 책입니다.", "달출판사", LocalDate.of(2026, 1, 1),
                "978-89-91995-00-1");
        String normalizedIsbn = request.isbn().trim().replace("-", "");
        BookResponse bookResponse = new BookResponse(UUID.randomUUID(), request.title(), request.author(),
                request.description(), request.publisher(), request.publishedDate(), normalizedIsbn,
                null, 0, BigDecimal.ZERO, Instant.now(), Instant.now());

        when(bookRepository.existsByIsbnAndDeletedAtIsNull(normalizedIsbn)).thenReturn(false);
        when(bookMapper.toBookDto(any(Book.class))).thenReturn(bookResponse);

        // when
        BookResponse book = bookServiceImpl.createBook(request, null);

        // then
        assertThat(book.title()).isEqualTo(request.title());
        assertThat(book.author()).isEqualTo(request.author());
        assertThat(book.isbn()).isEqualTo(normalizedIsbn);
        verify(bookRepository).save(any(Book.class));
        verify(bookMapper).toBookDto(any(Book.class));
    }

    @Test
    @DisplayName("isbn 중복으로 인한 도서 등록 실패")
    void create_book_fail_duplicate_isbn(){
        // given
        BookCreateRequest request = new BookCreateRequest("달선이의 하루", "달선", "달선이의 하루를 담은 책입니다.", "달출판사", LocalDate.of(2026, 1, 1),
                "978-89-91995-00-1");

        String normalizedIsbn = request.isbn().trim().replace("-", "");

        when(bookRepository.existsByIsbnAndDeletedAtIsNull(normalizedIsbn)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> bookServiceImpl.createBook(request, null))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.DUPLICATE_ISBN);

        verify(bookRepository, never()).save(any(Book.class));
        verify(bookMapper, never()).toBookDto(any(Book.class));

    }
}