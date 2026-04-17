package com.codeit.team4.deokhugam.book.controller;

import com.codeit.team4.deokhugam.book.dto.BookCreateRequest;
import com.codeit.team4.deokhugam.book.dto.BookResponse;
import com.codeit.team4.deokhugam.book.dto.BookUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Book", description = "도서 관련 API")
public interface BookApi {

    @Operation(summary = "도서 등록")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "도서 등록 성공",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청(입력값 검증 실패)"),
            @ApiResponse(responseCode = "409", description = "ISBN 중복")
    })
    ResponseEntity<BookResponse> createBook(
        @RequestPart("bookData") @Valid BookCreateRequest request,
                @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage);


    @Operation(summary = "도서 수정")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "도서 수정 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청(입력값 검증 실패)"),
            @ApiResponse(responseCode = "404", description = "도서 정보 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    ResponseEntity<BookResponse> updateBook(
        @PathVariable UUID bookId,
        @RequestPart("bookData") @Valid BookUpdateRequest request,
        @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage);


}
