package com.codeit.team4.deokhugam.book.service;

import com.codeit.team4.deokhugam.book.dto.BookCreateRequest;
import com.codeit.team4.deokhugam.book.dto.BookDto;
import com.codeit.team4.deokhugam.book.entity.Book;
import com.codeit.team4.deokhugam.book.mapper.BookMapper;
import com.codeit.team4.deokhugam.book.repository.BookRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    public BookDto createBook(BookCreateRequest request) {
        // isbn 중복 체크
        if(request.isbn() != null && bookRepository.existsByIsbnAndDeletedAtIsNull(request.isbn())){
            throw new IllegalArgumentException();
        }

        // 엔티티 생성
        Book book = new Book(request.title(), request.author(), request.description(),
                request.publisher(), request.publishedDate(), request.isbn());

        // db 저장
        bookRepository.save(book);

        // dto 변환
        BookDto bookDto = bookMapper.toBookDto(book);

        return bookDto;
    }
}
