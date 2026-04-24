package com.codeit.team4.deokhugam.book.controller;

import com.codeit.team4.deokhugam.book.dto.BookCreateRequest;
import com.codeit.team4.deokhugam.book.dto.BookResponse;
import com.codeit.team4.deokhugam.book.dto.BookUpdateRequest;
import com.codeit.team4.deokhugam.book.service.BookService;
import com.codeit.team4.deokhugam.book.service.BookQueryService;
import com.codeit.team4.deokhugam.global.response.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books")
@Slf4j
@Validated
public class BookController implements BookApi {

    private final BookService bookService;
    private final BookQueryService bookQueryService;

    private static final int MAX_BOOK_LIST_LIMIT = 100;

    // 도서 등록
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BookResponse> createBook(
            @RequestPart("bookData") @Valid BookCreateRequest request,
            @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage) {
        log.info("도서 등록 요청: title={}", request.title());
        BookResponse result = bookService.createBook(request, thumbnailImage);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    // 도서 수정
    @PatchMapping(value = "/{bookId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BookResponse> updateBook(
            @PathVariable UUID bookId,
            @RequestPart("bookData") @Valid BookUpdateRequest request,
            @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage) {
        log.info("도서 수정 요청: bookId={}", bookId);
        BookResponse result = bookService.updateBook(bookId, request, thumbnailImage);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    // 도서 논리 삭제
    @DeleteMapping("/{bookId}")
    public ResponseEntity<Void> deleteBook(
            @PathVariable UUID bookId
    ) {
        log.info("도서 삭제 요청: bookId={}", bookId);
        bookService.deleteBook(bookId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }

    // 도서 물리 삭제
    @DeleteMapping("/{bookId}/hard")
    public ResponseEntity<Void> hardDeleteBook(
            @PathVariable UUID bookId
    ) {
        log.info("도서 물리 삭제 요청: bookId={}", bookId);
        bookService.hardDeleteBook(bookId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }

    // 도서 정보 조회
    @GetMapping("/{bookId}")
    public ResponseEntity<BookResponse> getBook(
            @PathVariable UUID bookId
    ) {
        log.info("도서 정보 조회 요청: bookId={}", bookId);
        BookResponse result = bookService.getBook(bookId);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    // 도서 목록 조회
    @GetMapping
    public ResponseEntity<PageResponse<BookResponse>> getBookList(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "title") String orderBy,
            @RequestParam(defaultValue = "DESC") String direction,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Instant after,
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) int limit
    ) {
        log.info("도서 목록 조회 요청: keyword={}, orderBy={}, direction={}, limit={}",
                keyword, orderBy, direction, limit);
        PageResponse<BookResponse> books = bookQueryService.getBooks(keyword, orderBy, direction,
                cursor, after, limit);
        return ResponseEntity.status(HttpStatus.OK).body(books);
    }

    // ISBN으로 도서 정보 조회
    @GetMapping("/search")
    public ResponseEntity<BookResponse> searchByIsbn(
            @RequestParam
            @NotBlank
            @Pattern(regexp = "^(?:\\d{10}|\\d{13})$", message = "ISBN은 10자리 또는 13자리 숫자여야 합니다.")
            String isbn) {
        log.info("ISBN으로 도서 검색 요청: isbn={}", isbn);
        BookResponse result = bookService.searchByIsbn(isbn);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    // OCR ISBN 인식
    @PostMapping(value = "/isbn/ocr", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> extractIsbnFromImage(
            @RequestPart("image") MultipartFile image) {
        log.info("이미지 OCR ISBN 추출 요청");
        String isbn = bookService.extractIsbnFromImage(image);
        return ResponseEntity.ok(isbn);
    }
}