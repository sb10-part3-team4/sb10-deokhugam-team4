package com.codeit.team4.deokhugam.book.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.codeit.team4.deokhugam.book.dto.BookCreateRequest;
import com.codeit.team4.deokhugam.book.dto.BookResponse;
import com.codeit.team4.deokhugam.book.dto.BookUpdateRequest;
import com.codeit.team4.deokhugam.book.entity.Book;
import com.codeit.team4.deokhugam.book.repository.BookRepository;
import com.codeit.team4.deokhugam.config.TestContainerConfig;
import com.codeit.team4.deokhugam.global.error.BusinessException;
import com.codeit.team4.deokhugam.global.error.ErrorCode;
import java.time.LocalDate;
import java.util.List;
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
        BookCreateRequest createRequest = new BookCreateRequest("달선이의 하루", "달선",
                "달선이의 하루를 담은 책입니다.",
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
        // ISBN은 수정 대상이 아니므로 변경되지 않아야 함
        assertThat(updatedBook.getIsbn())
                .isEqualTo(createRequest.isbn().replace("-", ""));
    }

    @Test
    @DisplayName("존재하지 않는 도서 수정 실패")
    void update_fail_not_found() {
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

    @Test
    @DisplayName("도서 논리 삭제 성공")
    void delete_success() {
        // given
        BookCreateRequest request = new BookCreateRequest("달선이의 하루", "달선", "달선이의 하루를 담은 책입니다.",
                "달출판사", LocalDate.of(2026, 1, 1),
                "978-89-91995-00-1");
        BookResponse result = bookService.createBook(request, null);

        // when
        bookService.deleteBook(result.id());

        // then
        // DB 확인
        assertThat(bookRepository.findById(result.id()))
                .hasValueSatisfying(book -> assertThat(book.getDeletedAt()).isNotNull());
        assertThat(bookRepository.findByIdAndDeletedAtIsNull(result.id())).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 도서로 인한 도서 논리 삭제 실패")
    void delete_fail_not_found() {
        // given
        UUID bookId = UUID.randomUUID();

        // when & then
        assertThatThrownBy(() -> bookService.deleteBook(bookId))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.BOOK_NOT_FOUND);
    }

    @Test
    @DisplayName("도서 물리 삭제 성공")
    void permanent_delete_success() {
        // given
        BookCreateRequest request = new BookCreateRequest("달선이의 하루", "달선", "달선이의 하루를 담은 책입니다.",
                "달출판사", LocalDate.of(2026, 1, 1),
                "978-89-91995-00-1");
        BookResponse result = bookService.createBook(request, null);

        // when
        bookService.permanentDeleteBook(result.id());

        // then
        // DB 확인
        assertThat(bookRepository.findById(result.id())).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 도서로 인한 도서 물리 삭제 실패")
    void permanent_delete_fail_not_found() {
        // given
        UUID bookId = UUID.randomUUID();

        // when & then
        assertThatThrownBy(() -> bookService.permanentDeleteBook(bookId))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.BOOK_NOT_FOUND);
    }

    @Test
    @DisplayName("도서 정보 조회 성공")
    void get_detail_success() {
        // given
        BookCreateRequest request = new BookCreateRequest("달선이의 하루", "달선", "달선이의 하루를 담은 책입니다.",
                "달출판사", LocalDate.of(2026, 1, 1),
                "978-89-91995-00-1");
        BookResponse book = bookService.createBook(request, null);
        UUID bookId = book.id();

        // when
        BookResponse result = bookService.getBook(bookId);

        // then
        assertThat(result.id()).isEqualTo(bookId);
        assertThat(result.title()).isEqualTo(book.title());
        assertThat(result.author()).isEqualTo(book.author());

        // DB 확인
        Book savedBook = bookRepository.findByIdAndDeletedAtIsNull(bookId).orElseThrow();
        assertThat(savedBook.getId()).isEqualTo(result.id());
        assertThat(savedBook.getTitle()).isEqualTo(result.title());
        assertThat(savedBook.getAuthor()).isEqualTo(result.author());
    }

    @Test
    @DisplayName("존재하지 않는 도서로 인한 조회 실패")
    void get_detail_fail_not_found() {
        // given
        UUID bookId = UUID.randomUUID();

        // when & then
        assertThatThrownBy(() -> bookService.getBook(bookId))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.BOOK_NOT_FOUND);
    }

    @Test
    @DisplayName("도서 목록 조회 성공")
    void get_list_success() {
        // given
        BookCreateRequest request1 = new BookCreateRequest("달선이의 하루", "달선", "달선이의 하루를 담은 책입니다.",
                "달출판사", LocalDate.of(2026, 1, 1),
                "978-89-91995-00-1");
        BookResponse result1 = bookService.createBook(request1, null);

        BookCreateRequest request2 = new BookCreateRequest("달룡이의 하루", "달룡", "달룡이의 하루를 담은 책입니다.",
                "달출판사", LocalDate.of(2025, 1, 1),
                "978-89-91555-00-1");
        BookResponse result2 = bookService.createBook(request2, null);

        BookCreateRequest request3 = new BookCreateRequest("달례의 하루", "달례", "달례의 하루를 담은 책입니다.",
                "달출판사", LocalDate.of(2024, 1, 1),
                "978-89-91333-00-1");
        BookResponse result3 = bookService.createBook(request3, null);


        // when
        List<BookResponse> bookList = bookService.getBooks();

        // then
        assertThat(bookList.size()).isEqualTo(3);
    }
}
