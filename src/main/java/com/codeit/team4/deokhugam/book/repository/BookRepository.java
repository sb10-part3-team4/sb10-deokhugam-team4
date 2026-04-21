package com.codeit.team4.deokhugam.book.repository;

import com.codeit.team4.deokhugam.book.dto.BookResponse;
import com.codeit.team4.deokhugam.book.entity.Book;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jooq.DSLContext;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, UUID>, BookRepositoryCustom{

    Optional<Book> findByIdAndDeletedAtIsNull(UUID uuid);
    boolean existsByIsbnAndDeletedAtIsNull(String isbn);
}
