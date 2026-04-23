package com.codeit.team4.deokhugam.book.service;

import com.codeit.team4.deokhugam.book.dto.BookCreateRequest;
import com.codeit.team4.deokhugam.book.dto.BookResponse;
import com.codeit.team4.deokhugam.book.dto.BookUpdateRequest;
import com.codeit.team4.deokhugam.book.entity.Book;
import com.codeit.team4.deokhugam.book.mapper.BookMapper;
import com.codeit.team4.deokhugam.book.repository.BookRepository;
import com.codeit.team4.deokhugam.global.error.BusinessException;
import com.codeit.team4.deokhugam.global.error.ErrorCode;
import com.codeit.team4.deokhugam.naver.NaverBookClient;
import com.codeit.team4.deokhugam.naver.NaverBookResponse;
import com.codeit.team4.deokhugam.s3.S3Service;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final NaverBookClient naverBookClient;
    private final S3Service s3Service;

    @Transactional
    public BookResponse createBook(BookCreateRequest request, MultipartFile thumbnailImage) {

        // isbn 정규화
        String isbn = request.isbn() != null ? request.isbn().trim().replace("-", "") : null;

        // isbn 중복 체크
        if (isbn != null && bookRepository.existsByIsbnAndDeletedAtIsNull(isbn)) {
            throw new BusinessException(ErrorCode.DUPLICATE_ISBN, "isbn=" + isbn);
        }

        String thumbnailUrl = null;
        if (thumbnailImage != null && !thumbnailImage.isEmpty()) {
            thumbnailUrl = s3Service.upload(thumbnailImage);
        }

        // 엔티티 생성
        Book book = new Book(request.title(), request.author(), request.description(),
                request.publisher(), request.publishedDate(), isbn, thumbnailUrl);

        // db 저장, 동시 요청 대비 save 시 catch
        try {
            bookRepository.save(book);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.DUPLICATE_ISBN, "isbn=" + isbn);
        }

        log.info("도서 등록 완료: bookId={}", book.getId());

        // dto 변환
        BookResponse bookResponse = bookMapper.toResponse(book);
        return bookResponse;
    }

    @Transactional(readOnly = true)
    public Book findById(UUID bookId) {
        return bookRepository.findByIdAndDeletedAtIsNull(bookId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.BOOK_NOT_FOUND, "bookId=" + bookId));
    }

    @Transactional
    public BookResponse updateBook(UUID bookId, BookUpdateRequest request,
            MultipartFile thumbnailImage) {
        // 해당 id의 도서 찾기
        Book book = bookRepository.findByIdAndDeletedAtIsNull(bookId)
                .orElseThrow(() -> {
                    return new BusinessException(ErrorCode.BOOK_NOT_FOUND, "bookId=" + bookId);
                });

        // 썸네일 업로드
        String thumbnailUrl = null;
        if (thumbnailImage != null && !thumbnailImage.isEmpty()) {
            // 기존 썸네일 삭제
            if (book.getThumbnailUrl() != null) {
                s3Service.delete(book.getThumbnailUrl());
            }
            thumbnailUrl = s3Service.upload(thumbnailImage);
        }

        // update
        book.update(request.title(), request.author(), request.description(), request.publisher(),
                request.publishedDate(), thumbnailUrl != null ? thumbnailUrl : book.getThumbnailUrl());
        log.info("도서 수정 완료: bookId={}", bookId);

        BookResponse bookResponse = bookMapper.toResponse(book);
        return bookResponse;
    }

    @Transactional
    public void deleteBook(UUID bookId) {
        Book book = bookRepository.findByIdAndDeletedAtIsNull(bookId)
                .orElseThrow(
                        () -> new BusinessException(ErrorCode.BOOK_NOT_FOUND, "bookId=" + bookId));

        book.softDelete(Instant.now());
        log.info("도서 삭제 완료: bookId={}", bookId);
    }

    @Transactional
    public void hardDeleteBook(UUID bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(
                        () -> new BusinessException(ErrorCode.BOOK_NOT_FOUND, "bookId=" + bookId));

        bookRepository.delete(book);
        log.info("도서 물리 삭제 완료: bookId={}", bookId);
    }

    @Transactional(readOnly = true)
    public BookResponse getBook(UUID bookId) {
        Book book = bookRepository.findByIdAndDeletedAtIsNull(bookId)
                .orElseThrow(
                        () -> new BusinessException(ErrorCode.BOOK_NOT_FOUND, "bookId=" + bookId)
                );

        BookResponse bookResponse = bookMapper.toResponse(book);
        log.info("도서 단건 조회 완료: bookId={}", bookId);
        return bookResponse;
    }

    @Transactional(readOnly = true)
    public BookResponse searchByIsbn(String isbn) {
        NaverBookResponse response = naverBookClient.searchByIsbn(isbn);

        if (response == null || response.items() == null || response.items().isEmpty()) {
            throw new BusinessException(ErrorCode.BOOK_NOT_FOUND, "isbn=" + isbn);
        }

        NaverBookResponse.NaverBookItem item = response.items().get(0);

        log.info("ISBN으로 도서 검색 완료: isbn={}", isbn);

        return bookMapper.toBookResponse(item);
    }
}