package com.codeit.team4.deokhugam.book.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codeit.team4.deokhugam.book.dto.BookCreateRequest;
import com.codeit.team4.deokhugam.book.dto.BookResponse;
import com.codeit.team4.deokhugam.book.dto.BookUpdateRequest;
import com.codeit.team4.deokhugam.book.service.BookService;
import com.codeit.team4.deokhugam.global.config.AppProperties;
import com.codeit.team4.deokhugam.global.error.BusinessException;
import com.codeit.team4.deokhugam.global.error.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockPart;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BookController.class)
@Import(AppProperties.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("도서 수정 성공")
    void update_success() throws Exception {
        // given
        UUID bookId = UUID.randomUUID();
        BookUpdateRequest request = new BookUpdateRequest("달룡이의 하루", "달룡", "달룡이의 하루를 담은 책입니다.",
                "달출판사", LocalDate.of(2025, 1, 1));
        BookResponse response = new BookResponse(
                bookId, "달룡이의 하루", "달룡", "달룡이의 하루를 담은 책입니다.",
                "달출판사", LocalDate.of(2025, 1, 1), null, null, 0, BigDecimal.ZERO,
                Instant.now(), Instant.now());

        given(bookService.updateBook(eq(bookId), any(BookUpdateRequest.class), any())).willReturn(
                response);

        // when & then
        MockPart bookDataPart = new MockPart("bookData", objectMapper.writeValueAsBytes(request));
        bookDataPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart(HttpMethod.PATCH, "/api/books/{bookId}", bookId)
                        .part(bookDataPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("달룡이의 하루"))
                .andExpect(jsonPath("$.author").value("달룡"));
    }

    @Test
    @DisplayName("제목 누락으로 수정 실패")
    void update_fail_blank_title() throws Exception {
        // given
        UUID bookId = UUID.randomUUID();
        BookUpdateRequest request = new BookUpdateRequest("", "달룡", "달룡이의 하루를 담은 책입니다.",
                "달출판사", LocalDate.of(2025, 1, 1));

        // when & then
        MockPart bookDataPart = new MockPart("bookData", objectMapper.writeValueAsBytes(request));
        bookDataPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart(HttpMethod.PATCH, "/api/books/{bookId}", bookId)
                        .part(bookDataPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"))
                .andExpect(
                        jsonPath("$.message").value(org.hamcrest.Matchers.containsString("title")));
    }

    @Test
    @DisplayName("존재하지 않는 도서 수정 실패")
    void update_fail_not_found() throws Exception {
        // given
        UUID bookId = UUID.randomUUID();
        BookUpdateRequest request = new BookUpdateRequest("달룡이의 하루", "달룡", "달룡이의 하루를 담은 책입니다.",
                "달출판사", LocalDate.of(2025, 1, 1));

        given(bookService.updateBook(eq(bookId), any(BookUpdateRequest.class), any()))
                .willThrow(new BusinessException(ErrorCode.BOOK_NOT_FOUND));

        // when & then
        MockPart bookDataPart = new MockPart("bookData", objectMapper.writeValueAsBytes(request));
        bookDataPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart(HttpMethod.PATCH, "/api/books/{bookId}", bookId)
                        .part(bookDataPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("BOOK_NOT_FOUND"));
    }
}