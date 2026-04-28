-- review_statistics (리뷰별 집계: 댓글 수)
CREATE TABLE review_statistics
(
    review_id     UUID PRIMARY KEY REFERENCES reviews (id) ON DELETE CASCADE,
    comment_count INT NOT NULL DEFAULT 0
);

-- book_statistics 백필 (미배포 시 V3에, 배포 완료 시 새 버전으로)
FROM reviews r
LEFT JOIN comments c
  ON c.review_id = r.id
 AND c.deleted_at IS NULL
WHERE r.deleted_at IS NULL
GROUP BY r.id;