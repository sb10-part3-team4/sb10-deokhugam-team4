package com.codeit.team4.deokhugam.book.service;

import com.codeit.team4.deokhugam.book.dto.BookCreateRequest;
import com.codeit.team4.deokhugam.book.dto.BookResponse;
import com.codeit.team4.deokhugam.book.entity.Book;
import com.codeit.team4.deokhugam.book.mapper.BookMapper;
import com.codeit.team4.deokhugam.book.repository.BookRepository;
import com.codeit.team4.deokhugam.global.error.BusinessException;
import com.codeit.team4.deokhugam.global.error.ErrorCode;
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

    @Transactional
    public BookResponse createBook(BookCreateRequest request, MultipartFile thumbnailImage) {
        log.info("도서 등록 시작: title={}", request.title());

        // isbn 정규화
        String isbn = request.isbn() != null ? request.isbn().trim().replace("-", "") : null;

        // isbn 중복 체크
        if(isbn != null && bookRepository.existsByIsbnAndDeletedAtIsNull(request.isbn())){
            log.warn("도서 등록 실패 - ISBN 중복: isbn={}", isbn);
            throw new BusinessException(ErrorCode.DUPLICATE_ISBN);
        }

        // 엔티티 생성
        Book book = new Book(request.title(), request.author(), request.description(),
                request.publisher(), request.publishedDate(), isbn);

        // db 저장, 동시 요청 대비 save 시 catch
        try {
            bookRepository.save(book);
        } catch (DataIntegrityViolationException e){
            throw new BusinessException(ErrorCode.DUPLICATE_ISBN);
        }

        log.info("도서 등록 완료: bookId={}", book.getId());

        // dto 변환
        BookResponse bookResponse = bookMapper.toBookDto(book);
        return bookResponse;
    }
}
