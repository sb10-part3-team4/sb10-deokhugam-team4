package com.codeit.team4.deokhugam.book.service;

import com.codeit.team4.deokhugam.book.dto.BookCreateRequest;
import com.codeit.team4.deokhugam.book.dto.BookResponse;
import com.codeit.team4.deokhugam.book.dto.BookUpdateRequest;
import com.codeit.team4.deokhugam.book.dto.NaverBookSearchResponse;
import com.codeit.team4.deokhugam.book.entity.Book;
import com.codeit.team4.deokhugam.book.mapper.BookMapper;
import com.codeit.team4.deokhugam.book.repository.BookRepository;
import com.codeit.team4.deokhugam.global.error.BusinessException;
import com.codeit.team4.deokhugam.global.error.ErrorCode;
import com.codeit.team4.deokhugam.global.lock.DistributedLock;
import com.codeit.team4.deokhugam.naver.NaverBookClient;
import com.codeit.team4.deokhugam.naver.NaverBookResponse;
import com.codeit.team4.deokhugam.ocr.OcrSpaceClient;
import com.codeit.team4.deokhugam.s3.S3Service;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final NaverBookClient naverBookClient;
    private final S3Service s3Service;
    private final OcrSpaceClient ocrSpaceClient;
    private static final Pattern ISBN_PATTERN = Pattern.compile(
            "(?:ISBN[:\\s-]*)?(97[89][\\d-]{10,17})|(\\d{9}[\\dXx])");

    @DistributedLock(key = "deokhugam:book:isbn", lockParam = "request.isbn")
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

    @Transactional(readOnly = true)
    public List<Book> findAllByIds(List<UUID> bookIds) {
        return bookRepository.findAllById(bookIds);
    }

    @DistributedLock(key = "deokhugam:book", lockParam = {"bookId"})
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
        String oldThumbnailUrl = book.getThumbnailUrl();    // 기존에 등록되어 있던 썸네일 URL 보관

        // 새로운 이미지 파일이 넘어온 경우에만 S3 업로드 수행
        if (thumbnailImage != null && !thumbnailImage.isEmpty()) {
            thumbnailUrl = s3Service.upload(thumbnailImage);
        }

        // 도서 정보 업데이트 (Dirty Checking 활용)
        book.update(request.title(), request.author(), request.description(), request.publisher(),
                request.publishedDate(),
                thumbnailUrl != null ? thumbnailUrl : book.getThumbnailUrl());
        log.info("도서 수정 완료: bookId={}", bookId);

        // 3. 트랜잭션 상태에 따른 S3 파일 사후 관리
        if (thumbnailUrl != null) {
            String uploadedThumbnailUrl = thumbnailUrl;

            // 현재 진행 중인 DB 트랜잭션에 사후 작업(Synchronization)을 등록
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {

                        // DB 업데이트가 최종 성공했을 때
                        @Override
                        public void afterCommit() {
                            // 새 이미지가 잘 올라갔으니, 더 이상 필요 없는 '기존 이미지'를 S3에서 삭제
                            if (oldThumbnailUrl != null) {
                                s3Service.delete(oldThumbnailUrl);
                            }
                        }

                        // 트랜잭션이 종료되었을 때 (성공/실패 모두 포함)
                        @Override
                        public void afterCompletion(int status) {
                            if (status == STATUS_ROLLED_BACK) {
                                s3Service.delete(uploadedThumbnailUrl); // 롤백되면 새로 올린 것도 삭제
                            }
                        }
                    });
        }

        BookResponse bookResponse = bookMapper.toResponse(book);
        return bookResponse;
    }

    @DistributedLock(key = "deokhugam:book", lockParam = {"bookId"})
    @Transactional
    public void deleteBook(UUID bookId) {
        Book book = bookRepository.findByIdAndDeletedAtIsNull(bookId)
                .orElseThrow(
                        () -> new BusinessException(ErrorCode.BOOK_NOT_FOUND, "bookId=" + bookId));

        book.softDelete(Instant.now());
        log.info("도서 삭제 완료: bookId={}", bookId);
    }

    @DistributedLock(key = "deokhugam:book", lockParam = {"bookId"})
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
    public NaverBookSearchResponse searchByIsbn(String isbn) {
        NaverBookResponse response = naverBookClient.searchByIsbn(isbn);

        if (response == null || response.items() == null || response.items().isEmpty()) {
            throw new BusinessException(ErrorCode.BOOK_NOT_FOUND, "isbn=" + isbn);
        }

        NaverBookResponse.NaverBookItem item = response.items().get(0);

        log.info("ISBN으로 도서 검색 완료: isbn={}", isbn);

        String thumbnailBase64 = null;
        if (item.image() != null && !item.image().isEmpty()) {
            byte[] imageBytes = naverBookClient.fetchImageAsBytes(item.image());
            if (imageBytes != null) {
                thumbnailBase64 = Base64.getEncoder().encodeToString(imageBytes);
            }
        }

        return bookMapper.toNaverBookSearchResponse(item, thumbnailBase64);
    }

    public String extractIsbnFromImage(MultipartFile image) {
        // OCR Client를 통해 전체 텍스트 추출
        String text = ocrSpaceClient.extractText(image);

        Matcher matcher = ISBN_PATTERN.matcher(text);

        // 정규식 매칭 확인
        if (!matcher.find()) {
            throw new BusinessException(ErrorCode.OCR_ISBN_NOT_FOUND,
                    "textLength=" + text.length());
        }

        // 추출된 그룹 확인 및 공백/하이픈 제거
        String isbn = (matcher.group(1) != null ? matcher.group(1) : matcher.group(2))
                .replaceAll("[-\\s]", "");

        // 정규화 후 자릿수 재검증 (ISBN-10=10, ISBN-13=13)
        if (isbn.length() != 10 && isbn.length() != 13) {
            throw new BusinessException(ErrorCode.OCR_ISBN_NOT_FOUND, "isbn=" + isbn);
        }
        log.info("이미지에서 ISBN 추출 완료: isbn={}", isbn);
        return isbn;
    }
}