package com.codeit.team4.deokhugam.book.service.query;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.codeit.team4.deokhugam.book.dto.BookCreateRequest;
import com.codeit.team4.deokhugam.book.dto.BookResponse;
import com.codeit.team4.deokhugam.book.repository.BookRepository;
import com.codeit.team4.deokhugam.book.service.BookService;
import com.codeit.team4.deokhugam.config.TestContainerConfig;
import com.codeit.team4.deokhugam.global.error.BusinessException;
import com.codeit.team4.deokhugam.global.error.ErrorCode;
import com.codeit.team4.deokhugam.global.response.PageResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestContainerConfig.class)
class BookQueryServiceTest {

    @Autowired
    BookQueryService bookQueryService;

    @Autowired
    BookService bookService;

    @Autowired
    BookRepository bookRepository;

    @AfterEach
    void tearDown() {
        bookRepository.deleteAll();
    }

    @Test
    @DisplayName("도서 목록 조회 성공")
    void get_book_list_success() {
        // given
        createSampleBook();

        // when
        PageResponse<BookResponse> result = bookQueryService.getBooks(
                null, "title", "ASC", null, null, 50);

        // then
        assertThat(result.content()).hasSize(2);
        assertThat(result.hasNext()).isFalse();
        assertThat(result.totalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("title 키워드로 도서 목록 조회 성공")
    void get_book_list_with_keyword_success() {
        // given
        createSampleBook();

        // when
        PageResponse<BookResponse> result = bookQueryService.getBooks(
                "달선", "title", "ASC", null, null, 50);

        // then
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).title()).isEqualTo("달선이의 하루");
        assertThat(result.totalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("rating 정렬로 도서 목록 조회 성공")
    void get_book_list_order_by_rating_success() {
        // given
        createSampleBook();

        // when
        PageResponse<BookResponse> result = bookQueryService.getBooks(
                null, "rating", "DESC", null, null, 50);

        // then
        assertThat(result.content()).hasSize(2);
        assertThat(result.content().get(0).rating())
                .isGreaterThanOrEqualTo(result.content().get(1).rating());
    }

    @Test
    @DisplayName("커서로 다음 페이지 조회 시 중복/누락 없음 성공")
    void get_book_list_next_page_no_duplicate_success() {
        // given
        for (int i = 0; i < 5; i++) {
            bookService.createBook(new BookCreateRequest(
                    "책" + i, "저자" + i, "설명" + i, "출판사",
                    LocalDate.of(2026, 1, 1),
                    "978-89-6626-38" + i + "-5"), null);
        }

        // when
        PageResponse<BookResponse> firstPage = bookQueryService.getBooks(
                null, "title", "ASC", null, null, 3);
        PageResponse<BookResponse> secondPage = bookQueryService.getBooks(
                null, "title", "ASC",
                firstPage.nextCursor(), firstPage.nextAfter(), 3);

        // then
        assertThat(firstPage.hasNext()).isTrue();
        assertThat(firstPage.content()).hasSize(3);

        assertThat(secondPage.hasNext()).isFalse();
        assertThat(secondPage.content()).hasSize(2);

        // 중복 없음 확인
        List<UUID> firstIds = firstPage.content().stream().map(BookResponse::id).toList();
        List<UUID> secondIds = secondPage.content().stream().map(BookResponse::id).toList();
        assertThat(firstIds).doesNotContainAnyElementsOf(secondIds);
    }

    @Test
    @DisplayName("잘못된 orderBy로 도서 목록 조회 실패")
    void get_book_list_fail_invalid_order_by() {
        assertThatThrownBy(() -> bookQueryService.getBooks(
                null, "invalidSort", "ASC", null, null, 50))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_INPUT);
    }

    @Test
    @DisplayName("잘못된 direction으로 도서 목록 조회 실패")
    void get_book_list_fail_invalid_direction() {
        assertThatThrownBy(() -> bookQueryService.getBooks(
                null, "title", "abc", null, null, 50))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_INPUT);
    }

    @Test
    @DisplayName("cursor만 있고 after가 없으면 도서 목록 조회 실패")
    void get_book_list_fail_cursor_without_after() {
        assertThatThrownBy(() -> bookQueryService.getBooks(
                null, "title", "ASC", "cursor", null, 50))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_INPUT);
    }

    @Test
    @DisplayName("잘못된 cursor 포맷으로 도서 목록 조회 실패")
    void get_book_list_fail_invalid_cursor_format() {
        assertThatThrownBy(() -> bookQueryService.getBooks(
                null, "rating", "ASC", "abc", Instant.now(), 50))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_INPUT);
    }

    @Test
    @DisplayName("논리 삭제된 도서는 목록 조회에서 제외 성공")
    void get_book_list_exclude_deleted_success() {
        // given
        BookResponse created = bookService.createBook(new BookCreateRequest(
                "달선이의 하루", "달선", "달선이의 하루를 담은 책입니다.",
                "달출판사", LocalDate.of(2026, 1, 1), "978-89-91995-00-1"), null);
        bookService.deleteBook(created.id());

        // when
        PageResponse<BookResponse> result = bookQueryService.getBooks(
                null, "title", "ASC", null, null, 50);

        // then
        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isZero();
    }

    private void createSampleBook() {
        BookCreateRequest request1 = new BookCreateRequest("달선이의 하루", "달선", "달선이의 하루를 담은 책입니다.",
                "달출판사", LocalDate.of(2026, 1, 1),
                "978-89-91995-00-1");
        BookCreateRequest request2 = new BookCreateRequest("달룡이의 하루", "달룡", "달룡이의 하루를 담은 책입니다.",
                "달출판사", LocalDate.of(2025, 1, 1),
                "978-89-91555-00-1");
        bookService.createBook(request1, null);
        bookService.createBook(request2, null);
    }
}