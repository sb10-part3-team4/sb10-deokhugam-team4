-- review_statistics (리뷰별 집계: 댓글 수)
CREATE TABLE review_statistics
(
    review_id     UUID PRIMARY KEY REFERENCES reviews (id) ON DELETE CASCADE,
    comment_count INT NOT NULL DEFAULT 0
);