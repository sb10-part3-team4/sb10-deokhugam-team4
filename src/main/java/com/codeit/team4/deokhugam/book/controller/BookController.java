package com.codeit.team4.deokhugam.book.controller;

import com.codeit.team4.deokhugam.book.dto.BookCreateRequest;
import com.codeit.team4.deokhugam.book.dto.BookDto;
import com.codeit.team4.deokhugam.book.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books")
@Slf4j
public class BookController {

    private final BookService bookService;

    // 도서 등록
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BookDto> createBook(
            @RequestPart("bookData") @Valid BookCreateRequest request,
            @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage) {
        log.info("도서 등록 요청: title={}", request.title());
        BookDto result = bookService.createBook(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }



}
