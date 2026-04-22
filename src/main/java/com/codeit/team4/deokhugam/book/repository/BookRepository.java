package com.codeit.team4.deokhugam.book.repository;

import com.codeit.team4.deokhugam.book.entity.Book;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, UUID> {

    Optional<Book> findByIdAndDeletedAtIsNull(UUID uuid);
    boolean existsByIsbnAndDeletedAtIsNull(String isbn);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Book b SET b.reviewCount = b.reviewCount + 1 WHERE b.id = :bookId")
    void increaseReviewCount(@Param("bookId") UUID bookId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Book b SET b.reviewCount = b.reviewCount - 1 WHERE b.id = :bookId AND b.reviewCount > 0")
    void decreaseReviewCount(@Param("bookId") UUID bookId);
}
