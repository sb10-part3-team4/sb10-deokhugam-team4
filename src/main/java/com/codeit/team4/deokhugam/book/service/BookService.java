package com.codeit.team4.deokhugam.book.service;

import com.codeit.team4.deokhugam.book.dto.BookCreateRequest;
import com.codeit.team4.deokhugam.book.dto.BookDto;
import com.codeit.team4.deokhugam.book.entity.Book;
import com.codeit.team4.deokhugam.book.mapper.BookMapper;
import com.codeit.team4.deokhugam.book.repository.BookRepository;
import com.codeit.team4.deokhugam.global.error.BusinessException;
import com.codeit.team4.deokhugam.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    @Transactional
    public BookDto createBook(BookCreateRequest request) {
        log.info("도서 등록 시작: title={}", request.title());

        // isbn 중복 체크
        if(request.isbn() != null && bookRepository.existsByIsbnAndDeletedAtIsNull(request.isbn())){
            log.warn("도서 등록 실패 - ISBN 중복: isbn={}", request.isbn());
            throw new BusinessException(ErrorCode.DUPLICATE_ISBN);
        }

        // 엔티티 생성
        Book book = new Book(request.title(), request.author(), request.description(),
                request.publisher(), request.publishedDate(), request.isbn());

        // db 저장
        bookRepository.save(book);
        log.info("도서 등록 완료: bookId={}", book.getId());

        // dto 변환
        BookDto bookDto = bookMapper.toBookDto(book);
        return bookDto;
    }
}
