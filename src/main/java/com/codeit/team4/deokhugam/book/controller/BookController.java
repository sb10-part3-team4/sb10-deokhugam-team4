package com.codeit.team4.deokhugam.book.controller;

import com.codeit.team4.deokhugam.book.dto.BookCreateRequest;
import com.codeit.team4.deokhugam.book.dto.BookResponse;
import com.codeit.team4.deokhugam.book.dto.BookUpdateRequest;
import com.codeit.team4.deokhugam.book.service.BookService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books")
@Slf4j
public class BookController implements BookApi {

    private final BookService bookService;

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

    // 도서 삭제
    @DeleteMapping("/{bookId}")
    public ResponseEntity<Void> deleteBook(
            @PathVariable UUID bookId
    ) {
        log.info("도서 삭제 요청: bookId={}", bookId);
        bookService.deleteBook(bookId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }

}
