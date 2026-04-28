-- review_statistics (리뷰별 집계: 댓글 수)
CREATE TABLE review_statistics
(
    review_id     UUID PRIMARY KEY REFERENCES reviews (id) ON DELETE CASCADE,
    comment_count INT NOT NULL DEFAULT 0
);

-- 기존 리뷰들의 댓글 개수를 세어서 review_statistics 테이블에 백필(초기 데이터 삽입)
INSERT INTO review_statistics (review_id, comment_count)
SELECT r.id, COUNT(c.id)
FROM reviews r
         LEFT JOIN comments c
                   ON c.review_id = r.id
                       AND c.deleted_at IS NULL
WHERE r.deleted_at IS NULL
GROUP BY r.id;