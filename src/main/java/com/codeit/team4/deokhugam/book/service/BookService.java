package com.codeit.team4.deokhugam.book.service;

import com.codeit.team4.deokhugam.book.entity.Book;
import java.util.UUID;

public interface BookService {

    Book findById(UUID bookId);
}
