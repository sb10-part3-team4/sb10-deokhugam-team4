package com.codeit.team4.deokhugam.book.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeit.team4.deokhugam.book.dto.BookCreateRequest;
import com.codeit.team4.deokhugam.book.dto.BookResponse;
import com.codeit.team4.deokhugam.book.repository.BookRepository;
import com.codeit.team4.deokhugam.config.TestContainerConfig;
import com.codeit.team4.deokhugam.global.response.PageResponse;
import java.time.LocalDate;
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