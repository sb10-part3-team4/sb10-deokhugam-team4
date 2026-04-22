package com.codeit.team4.deokhugam.book.controller;

import com.codeit.team4.deokhugam.book.dto.BookCreateRequest;
import com.codeit.team4.deokhugam.book.dto.BookResponse;
import com.codeit.team4.deokhugam.book.dto.BookUpdateRequest;
import com.codeit.team4.deokhugam.global.error.ErrorResponse;
import com.codeit.team4.deokhugam.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Book", description = "도서 관련 API")
public interface BookApi {

    @Operation(summary = "도서 등록")
    @RequestBody(
            content = @Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    encoding = {
                            @Encoding(name = "bookData", contentType = MediaType.APPLICATION_JSON_VALUE),
                            @Encoding(name = "thumbnailImage", contentType = "image/*")
                    }
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "도서 등록 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청(입력값 검증 실패)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "ISBN 중복",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<BookResponse> createBook(
            @Parameter(description = "도서 등록 정보", required = true)
            @RequestPart("bookData") @Valid BookCreateRequest request,
            @Parameter(description = "썸네일 이미지 파일")
            @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage);

    @Operation(summary = "도서 수정")
    @RequestBody(
            content = @Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    encoding = {
                            @Encoding(name = "bookData", contentType = MediaType.APPLICATION_JSON_VALUE),
                            @Encoding(name = "thumbnailImage", contentType = "image/*")
                    }
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "도서 수정 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청(입력값 검증 실패)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "도서 정보 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<BookResponse> updateBook(
            @Parameter(description = "수정할 도서 ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID bookId,
            @Parameter(description = "도서 수정 정보", required = true)
            @RequestPart("bookData") @Valid BookUpdateRequest request,
            @Parameter(description = "썸네일 이미지 파일")
            @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage);

    @Operation(summary = "도서 논리 삭제")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "도서 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "도서 정보 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<Void> deleteBook(
            @Parameter(description = "삭제할 도서 ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID bookId);

    @Operation(summary = "도서 물리 삭제")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "도서 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "도서 정보 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<Void> permanentDeleteBook(
            @Parameter(description = "영구 삭제할 도서 ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID bookId);

    @Operation(summary = "도서 정보 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "도서 정보 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookResponse.class))),
            @ApiResponse(responseCode = "404", description = "도서 정보 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<BookResponse> getBook(@PathVariable UUID bookId);


    @Operation(summary = "도서 목록 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "도서 정보 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<PageResponse<BookResponse>> getBookList(
            @Parameter(description = "검색 키워드", example = "달선이의 하루")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "정렬 기준 컬럼", example = "title", required = true)
            @RequestParam(defaultValue = "title") String orderBy,
            @Parameter(description = "정렬 방향", example = "DESC", required = true)
            @RequestParam(defaultValue = "DESC") String direction,
            @Parameter(description = "커서 값 (이전 페이지 마지막 항목의 정렬값)", example = "달선이의 하루")
            @RequestParam(required = false) String cursor,
            @Parameter(description = "기준 시간", example = "2026-04-22T00:00:00Z")
            @RequestParam(required = false) Instant after,
            @Parameter(description = "조회 개수 (1~100)", example = "50", required = true)
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) int limit
    );
}