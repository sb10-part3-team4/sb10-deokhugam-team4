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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "도서 관리", description = "도서 관련 API")
public interface BookApi {

    @Operation(summary = "도서 등록", description = "새로운 도서를 등록합니다.")
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
            @ApiResponse(responseCode = "400", description = "잘못된 요청(입력값 검증 실패, ISBN 형식 오류 등)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "ISBN 중복",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<BookResponse> createBook(
            @Parameter(description = "도서 정보", required = true)
            @RequestPart("bookData") @Valid BookCreateRequest request,
            @Parameter(description = "도서 썸네일 이미지")
            @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage);

    @Operation(summary = "도서 정보 수정", description = "도서 정보를 수정합니다.")
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
            @Parameter(description = "도서 ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID bookId,
            @Parameter(description = "수정할 도서 정보", required = true)
            @RequestPart("bookData") @Valid BookUpdateRequest request,
            @Parameter(description = "수정할 도서 썸네일 이미지")
            @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage);

    @Operation(summary = "도서 논리 삭제", description = "도서를 논리적으로 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "도서 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "도서 정보 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<Void> deleteBook(
            @Parameter(description = "도서 ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID bookId);

    @Operation(summary = "도서 물리 삭제", description = "도서를 물리적으로 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "도서 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "도서 정보 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<Void> hardDeleteBook(
            @Parameter(description = "도서 ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID bookId);

    @Operation(summary = "도서 상세 정보 조회", description = "도서 ID로 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "도서 정보 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookResponse.class))),
            @ApiResponse(responseCode = "404", description = "도서 정보 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<BookResponse> getBook(
            @Parameter(description = "도서 ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID bookId);


    @Operation(summary = "도서 목록 조회", description = "검색 조건에 맞는 도서 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "도서 정보 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청(정렬 기준 오류, 페이지네이션 파라미터 오류 등)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<PageResponse<BookResponse>> getBookList(
            @Parameter(description = "도서 제목 | 저자 | ISBN", example = "달선이의 하루")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "정렬 기준 (title | publishedDate | rating | reviewCount)", example = "title")
            @RequestParam(defaultValue = "title") String orderBy,
            @Parameter(description = "정렬 방향", example = "DESC", schema = @Schema(type = "string", allowableValues = {
                    "ASC", "DESC"}))
            @RequestParam(defaultValue = "DESC") String direction,
            @Parameter(description = "커서 페이지네이션 커서")
            @RequestParam(required = false) String cursor,
            @Parameter(description = "보조 커서(createdAt)")
            @RequestParam(required = false) Instant after,
            @Parameter(description = "페이지 크기", example = "50")
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) int limit
    );

    @Operation(summary = "ISBN으로 도서 정보 조회", description = "Naver API를 통해 ISBN으로 도서 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "도서 정보 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BookResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 ISBN 형식",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "도서 정보 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<BookResponse> searchByIsbn(
            @Parameter(description = "ISBN 번호", example = "9788965402602")
            @RequestParam
            @NotBlank
            @Pattern(regexp = "^(?:\\d{10}\\d{13})$", message = "ISBN은 10자리 또는 13자리 숫자여야 합니다.")
            String isbn
    );
}