-- 더미 데이터 생성 (유저 1000명, 도서 500권, 리뷰 50000건, 좋아요/댓글/알림)
-- 실행: psql -h localhost -p 5433 -U deokhugam -d deokhugam -f k6/seed-data.sql

-- 1. 유저 1000명
INSERT INTO users (id, email, nickname, password, created_at, updated_at)
SELECT
    gen_random_uuid(),
    'user' || i || '@test.com',
    'testuser' || i,
    '$2a$10$dummyhashedpasswordforloadtesting000000000000000000',
    now() - (random() * interval '365 days'),
    now() - (random() * interval '30 days')
FROM generate_series(1, 1000) AS i
ON CONFLICT DO NOTHING;

-- 2. 도서 500권
INSERT INTO books (id, title, author, description, publisher, published_date, isbn, review_count, rating, created_at, updated_at)
SELECT
    gen_random_uuid(),
    '테스트 도서 ' || i || ' - ' || md5(random()::text),
    '저자 ' || (i % 100),
    '이것은 테스트 도서 ' || i || '의 설명입니다. 부하 테스트를 위해 생성된 더미 데이터입니다.',
    '출판사 ' || (i % 50),
    date '2020-01-01' + (random() * 1500)::int,
    '978' || lpad(i::text, 10, '0'),
    0,
    0,
    now() - (random() * interval '365 days'),
    now() - (random() * interval '30 days')
FROM generate_series(1, 500) AS i
ON CONFLICT DO NOTHING;

-- 3. 리뷰 50000건 (유저-도서 조합 랜덤, 중복 방지)
WITH user_ids AS (
    SELECT id, row_number() OVER () AS rn FROM users WHERE deleted_at IS NULL LIMIT 1000
),
book_ids AS (
    SELECT id, row_number() OVER () AS rn FROM books WHERE deleted_at IS NULL LIMIT 500
)
INSERT INTO reviews (id, book_id, user_id, content, rating, like_count, comment_count, created_at, updated_at)
SELECT
    gen_random_uuid(),
    b.id,
    u.id,
    '리뷰 내용입니다. 이 도서는 ' ||
        CASE (random() * 4)::int
            WHEN 0 THEN '매우 훌륭합니다. 강력 추천합니다.'
            WHEN 1 THEN '읽을만 합니다. 시간 날 때 읽어보세요.'
            WHEN 2 THEN '기대보다 별로였습니다. 아쉽네요.'
            WHEN 3 THEN '평범한 책입니다. 특별한 점은 없었습니다.'
            ELSE '최고의 책 중 하나입니다!'
        END,
    (random() * 4 + 1)::int,
    (random() * 20)::int,
    (random() * 5)::int,
    now() - (random() * interval '365 days'),
    now() - (random() * interval '30 days')
FROM user_ids u
CROSS JOIN book_ids b
WHERE random() < 0.1  -- 10% 확률로 조합 선택 → 약 50000건
ON CONFLICT DO NOTHING;

-- 4. books 테이블의 review_count, rating 갱신
UPDATE books SET
    review_count = sub.cnt,
    rating = sub.avg_rating,
    updated_at = now()
FROM (
    SELECT book_id, count(*) AS cnt, round(avg(rating), 2) AS avg_rating
    FROM reviews
    WHERE deleted_at IS NULL
    GROUP BY book_id
) sub
WHERE books.id = sub.book_id;

-- 5. book_statistics 갱신
INSERT INTO book_statistics (book_id, rating_sum, review_count)
SELECT book_id, sum(rating), count(*)
FROM reviews
WHERE deleted_at IS NULL
GROUP BY book_id
ON CONFLICT (book_id) DO UPDATE SET
    rating_sum = EXCLUDED.rating_sum,
    review_count = EXCLUDED.review_count;

-- 6. 좋아요 약 100000건
WITH review_ids AS (
    SELECT id, row_number() OVER () AS rn FROM reviews WHERE deleted_at IS NULL LIMIT 50000
),
user_ids AS (
    SELECT id, row_number() OVER () AS rn FROM users WHERE deleted_at IS NULL LIMIT 1000
)
INSERT INTO review_likes (id, review_id, user_id, created_at)
SELECT
    gen_random_uuid(),
    r.id,
    u.id,
    now() - (random() * interval '180 days')
FROM review_ids r
CROSS JOIN user_ids u
WHERE random() < 0.002  -- 약 100000건
ON CONFLICT DO NOTHING;

-- 7. 댓글 약 30000건
WITH review_ids AS (
    SELECT id FROM reviews WHERE deleted_at IS NULL LIMIT 50000
),
user_ids AS (
    SELECT id FROM users WHERE deleted_at IS NULL LIMIT 1000
)
INSERT INTO comments (id, review_id, user_id, content, created_at, updated_at)
SELECT
    gen_random_uuid(),
    r.id,
    (SELECT id FROM user_ids ORDER BY random() LIMIT 1),
    CASE (random() * 3)::int
        WHEN 0 THEN '공감합니다!'
        WHEN 1 THEN '좋은 리뷰 감사합니다.'
        WHEN 2 THEN '저도 비슷한 생각입니다.'
        ELSE '참고가 되었어요.'
    END,
    now() - (random() * interval '180 days'),
    now() - (random() * interval '30 days')
FROM review_ids r
WHERE random() < 0.6
ON CONFLICT DO NOTHING;

-- 8. 알림 약 10000건 (confirmed=false만 먼저, confirmed=true는 confirmed_at과 함께)
WITH review_data AS (
    SELECT r.id AS review_id, r.user_id, r.content,
           u.nickname
    FROM reviews r
    JOIN users u ON r.user_id = u.id
    WHERE r.deleted_at IS NULL
    LIMIT 20000
),
target_users AS (
    SELECT id FROM users WHERE deleted_at IS NULL LIMIT 1000
),
generated AS (
    SELECT
        gen_random_uuid() AS id,
        tu.id AS user_id,
        rd.review_id,
        left(rd.content, 100) AS review_content,
        rd.nickname || '님이 리뷰에 좋아요를 눌렀습니다.' AS message,
        random() < 0.7 AS is_confirmed,
        now() - (random() * interval '90 days') AS created_at,
        now() - (random() * interval '30 days') AS updated_at
    FROM review_data rd
    CROSS JOIN target_users tu
    WHERE random() < 0.0005
)
INSERT INTO notifications (id, user_id, review_id, review_content, message, confirmed, confirmed_at, created_at, updated_at)
SELECT
    id, user_id, review_id, review_content, message,
    is_confirmed,
    CASE WHEN is_confirmed THEN created_at + interval '1 hour' * (random() * 24) ELSE NULL END,
    created_at, updated_at
FROM generated
ON CONFLICT DO NOTHING;

-- 결과 확인
SELECT 'users' AS table_name, count(*) FROM users WHERE deleted_at IS NULL
UNION ALL SELECT 'books', count(*) FROM books WHERE deleted_at IS NULL
UNION ALL SELECT 'reviews', count(*) FROM reviews WHERE deleted_at IS NULL
UNION ALL SELECT 'review_likes', count(*) FROM review_likes
UNION ALL SELECT 'comments', count(*) FROM comments WHERE deleted_at IS NULL
UNION ALL SELECT 'notifications', count(*) FROM notifications
UNION ALL SELECT 'book_statistics', count(*) FROM book_statistics;