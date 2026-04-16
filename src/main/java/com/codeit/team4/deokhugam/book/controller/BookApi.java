package com.codeit.team4.deokhugam.book.controller;

import com.codeit.team4.deokhugam.book.dto.BookCreateRequest;
import com.codeit.team4.deokhugam.book.dto.BookResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Book", description = "도서 관련 API")
public interface BookApi {

    @Operation(summary = "도서 등록")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "도서 등록 성공",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookResponse.class))),
            @ApiResponse(responseCode = "400", description = "입력값 검증 실패"),
            @ApiResponse(responseCode = "409", description = "ISBN 중복")
    })
    ResponseEntity<BookResponse> createBook(
        @RequestPart("bookData") BookCreateRequest request,
                @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage);

}
