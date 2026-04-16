package com.codeit.team4.deokhugam.book.entity;

import com.codeit.team4.deokhugam.global.entity.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "books")
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

    public Book(String title, String author, String description, String publisher, LocalDate publishedDate, String isbn) {
        this.title = title;
        this.author = author;
        this.description = description;
        this.publisher = publisher;
        this.publishedDate = publishedDate;
        this.isbn = isbn;
        this.reviewCount = 0;
        this.rating = BigDecimal.ZERO;
    }

    public void updateTitle(String newTitle) {
        this.title = newTitle;
    }

    public void updateAuthor(String newAuthor) {
        this.author = newAuthor;
    }

    public void updatePublisher(String newPublisher) {
        this.publisher = newPublisher;
    }

    public void updateDescription(String newDescription) {
        this.description = newDescription;
    }

    public void updatePublishedDate(LocalDate newPublishedDate) {
        this.publishedDate = newPublishedDate;
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
