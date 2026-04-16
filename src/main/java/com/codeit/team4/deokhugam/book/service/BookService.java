package com.codeit.team4.deokhugam.book.service;

import com.codeit.team4.deokhugam.book.dto.BookCreateRequest;
import com.codeit.team4.deokhugam.book.dto.BookResponse;
import com.codeit.team4.deokhugam.book.dto.BookUpdateRequest;
import com.codeit.team4.deokhugam.book.entity.Book;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface BookService {

    Book findById(UUID bookId);

    BookResponse createBook(BookCreateRequest request, MultipartFile thumbnailImage);

    BookResponse updateBook(UUID bookId, BookUpdateRequest request, MultipartFile thumbnailImage);
}
