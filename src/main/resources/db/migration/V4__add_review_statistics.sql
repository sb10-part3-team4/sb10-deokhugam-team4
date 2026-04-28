-- book_statistics (도서 집계)
CREATE TABLE book_statistics (
    book_id       UUID PRIMARY KEY REFERENCES books(id) ON DELETE CASCADE,
    rating_sum    INT NOT NULL DEFAULT 0,
    review_count  INT NOT NULL DEFAULT 0
);
