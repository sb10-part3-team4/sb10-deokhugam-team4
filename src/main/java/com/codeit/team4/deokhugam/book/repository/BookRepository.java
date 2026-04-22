package com.codeit.team4.deokhugam.book.repository;

import com.codeit.team4.deokhugam.book.entity.Book;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, UUID> {

    Optional<Book> findByIdAndDeletedAtIsNull(UUID uuid);
    boolean existsByIsbnAndDeletedAtIsNull(String isbn);
}
