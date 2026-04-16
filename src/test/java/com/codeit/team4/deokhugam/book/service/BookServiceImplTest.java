package com.codeit.team4.deokhugam.book.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.codeit.team4.deokhugam.book.dto.BookCreateRequest;
import com.codeit.team4.deokhugam.book.dto.BookResponse;
import com.codeit.team4.deokhugam.book.dto.BookUpdateRequest;
import com.codeit.team4.deokhugam.book.entity.Book;
import com.codeit.team4.deokhugam.book.mapper.BookMapper;
import com.codeit.team4.deokhugam.book.repository.BookRepository;
import com.codeit.team4.deokhugam.config.TestContainerConfig;
import com.codeit.team4.deokhugam.global.error.BusinessException;
import com.codeit.team4.deokhugam.global.error.ErrorCode;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Import(TestContainerConfig.class)
@ActiveProfiles("test")
@Transactional
class BookServiceImplTest {

    @Autowired
    BookRepository bookRepository;

    @Autowired
    BookMapper bookMapper;

    @Autowired
    BookService bookService;

    @Test
    @DisplayName("도서 등록 성공")
    void create_success() {
        // given
        BookCreateRequest request = new BookCreateRequest("달선이의 하루", "달선", "달선이의 하루를 담은 책입니다.",
                "달출판사", LocalDate.of(2026, 1, 1),
                "978-89-91995-00-1");
        String normalizedIsbn = request.isbn().trim().replace("-", "");

        // when
        BookResponse result = bookService.createBook(request, null);

        // then
        assertThat(result.title()).isEqualTo(request.title());
        assertThat(result.author()).isEqualTo(request.author());
        assertThat(result.isbn()).isEqualTo(normalizedIsbn);

        // DB에 실제로 저장됐는지 확인
        Book savedBook = bookRepository.findByIdAndDeletedAtIsNull(result.id()).orElseThrow();
        assertThat(savedBook.getTitle()).isEqualTo(request.title());
        assertThat(savedBook.getAuthor()).isEqualTo(request.author());
    }

    @Test
    @DisplayName("isbn 중복으로 인한 도서 등록 실패")
    void create_fail_duplicate_isbn() {
        // given
        BookCreateRequest request = new BookCreateRequest("달선이의 하루", "달선", "달선이의 하루를 담은 책입니다.",
                "달출판사", LocalDate.of(2026, 1, 1),
                "978-89-91995-00-1");
        bookService.createBook(request, null);  // 먼저 등록

        // when & then
        assertThatThrownBy(() -> bookService.createBook(request, null))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.DUPLICATE_ISBN);
    }

    @Test
    @DisplayName("도서 수정 성공")
    void update_book_success() {
        // given
        BookCreateRequest createRequest = new BookCreateRequest("달선이의 하루", "달선", "달선이의 하루를 담은 책입니다.",
                "달출판사", LocalDate.of(2026, 1, 1),
                "978-89-91995-00-1");
        BookResponse createdBook = bookService.createBook(createRequest, null);
        UUID bookId = createdBook.id();

        BookUpdateRequest request = new BookUpdateRequest("달룡이의 하루", "달룡", "달룡이의 하루를 담은 책입니다.",
                "달출판사", LocalDate.of(2025, 1, 1));

        // when
        BookResponse result = bookService.updateBook(bookId, request, null);

        // then
        assertThat(result.id()).isEqualTo(bookId);
        assertThat(result.title()).isEqualTo(request.title());
        assertThat(result.author()).isEqualTo(request.author());
        assertThat(result.publisher()).isEqualTo(request.publisher());

        // DB에 실제로 수정됐는지 확인
        Book updatedBook = bookRepository.findByIdAndDeletedAtIsNull(bookId).orElseThrow();
        assertThat(updatedBook.getTitle()).isEqualTo(request.title());
        assertThat(updatedBook.getAuthor()).isEqualTo(request.author());
    }

    @Test
    @DisplayName("존재하지 않는 도서 수정 실패")
    void update_fail_not_found(){
        // given
        UUID bookId = UUID.randomUUID();

        BookUpdateRequest request = new BookUpdateRequest("달룡이의 하루", "달룡", "달룡이의 하루를 담은 책입니다.",
                "달출판사", LocalDate.of(2025, 1, 1));

        // when & then
        assertThatThrownBy(() -> bookService.updateBook(bookId, request, null))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.BOOK_NOT_FOUND);


    }
}
