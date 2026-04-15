CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- users
CREATE TABLE users (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email       VARCHAR(100) NOT NULL UNIQUE,
    nickname    VARCHAR(50)  NOT NULL,
    password    VARCHAR(255) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted_at  TIMESTAMPTZ
);
CREATE INDEX idx_users_deleted_at ON users (deleted_at) WHERE deleted_at IS NOT NULL;

-- books
CREATE TABLE books (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title           VARCHAR(255) NOT NULL,
    author          VARCHAR(100) NOT NULL,
    description     TEXT         NOT NULL,
    publisher       VARCHAR(100) NOT NULL,
    published_date  DATE         NOT NULL,
    isbn            VARCHAR(20),
    thumbnail_url   VARCHAR(500),
    review_count    INT          NOT NULL DEFAULT 0,
    rating          NUMERIC(3,2) NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted_at      TIMESTAMPTZ
);
CREATE UNIQUE INDEX uq_books_isbn_alive
    ON books (isbn)
    WHERE deleted_at IS NULL AND isbn IS NOT NULL;
CREATE INDEX idx_books_title         ON books (title);
CREATE INDEX idx_books_published_date ON books (published_date);
CREATE INDEX idx_books_rating        ON books (rating);
CREATE INDEX idx_books_review_count  ON books (review_count);

-- reviews
CREATE TABLE reviews (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    book_id        UUID         NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    user_id        UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content        TEXT         NOT NULL,
    rating         INT          NOT NULL CHECK (rating BETWEEN 1 AND 5),
    like_count     INT          NOT NULL DEFAULT 0,
    comment_count  INT          NOT NULL DEFAULT 0,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted_at     TIMESTAMPTZ
);
CREATE UNIQUE INDEX uq_reviews_book_user_alive
    ON reviews (book_id, user_id)
    WHERE deleted_at IS NULL;
CREATE INDEX idx_reviews_created_at ON reviews (created_at);
CREATE INDEX idx_reviews_rating     ON reviews (rating);
CREATE INDEX idx_reviews_user_id    ON reviews (user_id);

-- review_likes
CREATE TABLE review_likes (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    review_id   UUID        NOT NULL REFERENCES reviews(id) ON DELETE CASCADE,
    user_id     UUID        NOT NULL REFERENCES users(id)   ON DELETE CASCADE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_review_likes UNIQUE (review_id, user_id)
);
CREATE INDEX idx_review_likes_user_id ON review_likes (user_id);

-- comments
CREATE TABLE comments (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    review_id   UUID        NOT NULL REFERENCES reviews(id) ON DELETE CASCADE,
    user_id     UUID        NOT NULL REFERENCES users(id)   ON DELETE CASCADE,
    content     TEXT        NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at  TIMESTAMPTZ
);
CREATE INDEX idx_comments_review_created ON comments (review_id, created_at);
CREATE INDEX idx_comments_user_id        ON comments (user_id);

-- notifications
CREATE TABLE notifications (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID         NOT NULL REFERENCES users(id)   ON DELETE CASCADE,
    review_id       UUID         NOT NULL REFERENCES reviews(id) ON DELETE CASCADE,
    review_content  TEXT         NOT NULL,
    message         VARCHAR(255) NOT NULL,
    confirmed       BOOLEAN      NOT NULL DEFAULT false,
    confirmed_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_notifications_user_created ON notifications (user_id, created_at DESC);
CREATE INDEX idx_notifications_confirmed_at ON notifications (confirmed_at) WHERE confirmed = true;

-- popular_books (배치 스냅샷)
CREATE TABLE popular_books (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    book_id       UUID         REFERENCES books(id) ON DELETE SET NULL,
    title         VARCHAR(255) NOT NULL,
    author        VARCHAR(100) NOT NULL,
    thumbnail_url VARCHAR(500),
    period        VARCHAR(20)  NOT NULL CHECK (period IN ('DAILY','WEEKLY','MONTHLY','ALL_TIME')),
    rank          INT          NOT NULL,
    score         NUMERIC(10,4) NOT NULL,
    review_count  INT          NOT NULL,
    rating        NUMERIC(3,2) NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_popular_books UNIQUE (period, book_id, created_at)
);
CREATE INDEX idx_popular_books_period_rank ON popular_books (period, rank);

-- popular_reviews (배치 스냅샷)
CREATE TABLE popular_reviews (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    review_id            UUID         REFERENCES reviews(id) ON DELETE SET NULL,
    book_id              UUID         REFERENCES books(id)   ON DELETE SET NULL,
    user_id              UUID         REFERENCES users(id)   ON DELETE SET NULL,
    book_title           VARCHAR(255) NOT NULL,
    book_thumbnail_url   VARCHAR(500),
    user_nickname        VARCHAR(50)  NOT NULL,
    review_content       TEXT         NOT NULL,
    review_rating        INT          NOT NULL,
    period               VARCHAR(20)  NOT NULL CHECK (period IN ('DAILY','WEEKLY','MONTHLY','ALL_TIME')),
    rank                 INT          NOT NULL,
    score                NUMERIC(10,4) NOT NULL,
    like_count           INT          NOT NULL,
    comment_count        INT          NOT NULL,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_popular_reviews UNIQUE (period, review_id, created_at)
);
CREATE INDEX idx_popular_reviews_period_rank ON popular_reviews (period, rank);

-- power_users (배치 스냅샷)
CREATE TABLE power_users (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id           UUID         REFERENCES users(id) ON DELETE SET NULL,
    nickname          VARCHAR(50)  NOT NULL,
    period            VARCHAR(20)  NOT NULL CHECK (period IN ('DAILY','WEEKLY','MONTHLY','ALL_TIME')),
    rank              INT          NOT NULL,
    score             NUMERIC(10,4) NOT NULL,
    review_score_sum  NUMERIC(10,4) NOT NULL,
    like_count        INT          NOT NULL,
    comment_count     INT          NOT NULL,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_power_users UNIQUE (period, user_id, created_at)
);
CREATE INDEX idx_power_users_period_rank ON power_users (period, rank);