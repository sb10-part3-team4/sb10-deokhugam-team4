package com.codeit.team4.deokhugam.book.service;

import com.codeit.team4.deokhugam.book.dto.BookCreateRequest;
import com.codeit.team4.deokhugam.book.dto.BookResponse;
import com.codeit.team4.deokhugam.book.dto.BookUpdateRequest;
import com.codeit.team4.deokhugam.book.entity.Book;
import com.codeit.team4.deokhugam.book.mapper.BookMapper;
import com.codeit.team4.deokhugam.book.repository.BookRepository;
import com.codeit.team4.deokhugam.global.error.BusinessException;
import com.codeit.team4.deokhugam.global.error.ErrorCode;
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
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    @Override
    @Transactional
    public BookResponse createBook(BookCreateRequest request, MultipartFile thumbnailImage) {

        // isbn 정규화
        String isbn = request.isbn() != null ? request.isbn().trim().replace("-", "") : null;

        // isbn 중복 체크
        if (isbn != null && bookRepository.existsByIsbnAndDeletedAtIsNull(isbn)) {
            throw new BusinessException(ErrorCode.DUPLICATE_ISBN, "isbn=" + isbn);
        }

        // Todo : 썸네일 이미지 저장 로직 구현

        // 엔티티 생성
        Book book = new Book(request.title(), request.author(), request.description(),
                request.publisher(), request.publishedDate(), isbn);

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

    @Override
    @Transactional(readOnly = true)
    public Book findById(UUID bookId) {
        return bookRepository.findByIdAndDeletedAtIsNull(bookId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.BOOK_NOT_FOUND, "bookId=" + bookId));
    }

    @Override
    @Transactional
    public BookResponse updateBook(UUID bookId, BookUpdateRequest request,
            MultipartFile thumbnailImage) {
        // 해당 id의 도서 찾기
        Book book = bookRepository.findByIdAndDeletedAtIsNull(bookId)
                .orElseThrow(() -> {
                    return new BusinessException(ErrorCode.BOOK_NOT_FOUND, "bookId=" + bookId);
                });

        // update
        book.update(request.title(), request.author(), request.description(), request.publisher(),
                request.publishedDate());

        BookResponse bookResponse = bookMapper.toResponse(book);
        log.info("도서 수정 완료: bookId={}", bookId);
        return bookResponse;

        // todo: 썸네일 업데이트 로직 구현
    }

    @Override
    @Transactional
    public void deleteBook(UUID bookId) {
        Book book = bookRepository.findByIdAndDeletedAtIsNull(bookId)
                .orElseThrow(
                        () -> new BusinessException(ErrorCode.BOOK_NOT_FOUND, "bookId=" + bookId));

        book.softDelete(Instant.now());
        log.info("도서 삭제 완료: bookId={}", bookId);
    }

    @Override
    @Transactional
    public void permanentDeleteBook(UUID bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(
                        () -> new BusinessException(ErrorCode.BOOK_NOT_FOUND, "bookId=" + bookId));

        bookRepository.delete(book);
        log.info("도서 물리 삭제 완료: bookId={}", bookId);
    }

    @Override
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
}
