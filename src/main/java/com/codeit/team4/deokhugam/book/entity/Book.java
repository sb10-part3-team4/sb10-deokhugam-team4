package com.codeit.team4.deokhugam.book.entity;

import com.codeit.team4.deokhugam.global.entity.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
        name = "books",
        indexes = {
                @Index(name = "uq_books_isbn_alive", columnList = "isbn"),
                @Index(name = "idx_books_title", columnList = "title"),
                @Index(name = "idx_books_published_date", columnList = "published_date"),
                @Index(name = "idx_books_rating", columnList = "rating"),
                @Index(name = "idx_books_review_count", columnList = "review_count"),
                @Index(name = "idx_books_deleted_at", columnList = "deleted_at")
        }
)
@NoArgsConstructor
public class Book extends BaseUpdatableEntity {

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 100)
    private String author;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 100)
    private String publisher;

    @Column(nullable = false)
    private LocalDate publishedDate;

    @Column(length = 20)
    private String isbn;

    @Column(length = 500)
    private String thumbnailUrl;

    @Column(nullable = false)
    private int reviewCount;

    @Column(nullable = false)
    private BigDecimal rating;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    // 기존 생성자 유지
    public Book(String title, String author, String description, String publisher,
            LocalDate publishedDate, String isbn) {
        this(title, author, description, publisher, publishedDate, isbn, null);
    }

    // thumbnailUrl 포함 생성자
    public Book(String title, String author, String description, String publisher,
            LocalDate publishedDate, String isbn, String thumbnailUrl) {
        this.title = title;
        this.author = author;
        this.description = description;
        this.publisher = publisher;
        this.publishedDate = publishedDate;
        this.isbn = isbn;
        this.thumbnailUrl = thumbnailUrl;
        this.reviewCount = 0;
        this.rating = BigDecimal.ZERO;
    }

    // 기존 update 유지 (thumbnailUrl 없는 버전)
    public void update(String title, String author, String description,
            String publisher, LocalDate publishedDate) {
        this.update(title, author, description, publisher, publishedDate, this.thumbnailUrl);
    }

    // thumbnailUrl 포함 update
    public void update(String title, String author, String description,
            String publisher, LocalDate publishedDate, String thumbnailUrl) {
        this.title = title;
        this.author = author;
        this.description = description;
        this.publisher = publisher;
        this.publishedDate = publishedDate;
        this.thumbnailUrl = thumbnailUrl;
    }

    public void softDelete(Instant deletedAt){
        this.deletedAt = deletedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Book other)) {
            return false;
        }
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
