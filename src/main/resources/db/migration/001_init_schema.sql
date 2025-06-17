--auth
CREATE TABLE IF NOT EXISTS users
(
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(255),
    avatar_url    VARCHAR(255),
    city_id       BIGINT,
    email         VARCHAR(255) UNIQUE,
    password_hash VARCHAR(255),
    created_at    TIMESTAMP,
    is_banned     BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS sessions
(
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT,
    refresh_token VARCHAR(255),
    created_at    TIMESTAMP,
    expires_at    TIMESTAMP
);

--catalog
CREATE TABLE IF NOT EXISTS genres
(
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS books
(
    id                BIGSERIAL PRIMARY KEY,
    title             VARCHAR(255),
    author            VARCHAR(255),
    year              INTEGER,
    description       VARCHAR(2048),
    age_category      VARCHAR(32),
    created_by        BIGINT,
    moderation_status VARCHAR(32),
    created_at        TIMESTAMP
);

CREATE TABLE IF NOT EXISTS book_genres
(
    book_id  BIGINT NOT NULL,
    genre_id BIGINT NOT NULL,
    PRIMARY KEY (book_id, genre_id)
);

CREATE TABLE IF NOT EXISTS book_images
(
    id      BIGSERIAL PRIMARY KEY,
    book_id BIGINT       NOT NULL,
    url     VARCHAR(255) NOT NULL
);

--exchange
CREATE TABLE IF NOT EXISTS exchanges
(
    id                            BIGSERIAL PRIMARY KEY,
    sender_id                     BIGINT,
    receiver_id                   BIGINT,
    offered_listing_id            BIGINT,
    selected_listing_id           BIGINT,
    status                        VARCHAR(32),
    sender_confirmed_completion   BOOLEAN DEFAULT FALSE,
    receiver_confirmed_completion BOOLEAN DEFAULT FALSE,
    completed_at                  TIMESTAMP,
    created_at                    TIMESTAMP
);

--listings
CREATE TABLE IF NOT EXISTS cities
(
    id      BIGSERIAL PRIMARY KEY,
    name    VARCHAR(255),
    region  VARCHAR(255),
    country VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS listings
(
    id         BIGSERIAL PRIMARY KEY,
    owner_id   BIGINT,
    book_id    BIGINT,
    condition  VARCHAR(32),
    city_id    BIGINT NOT NULL,
    is_open    BOOLEAN DEFAULT TRUE,
    is_blocked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS listing_images
(
    id         BIGSERIAL PRIMARY KEY,
    listing_id BIGINT,
    url        VARCHAR(255)
);

--messaging
CREATE TABLE IF NOT EXISTS dialogs
(
    id         BIGSERIAL PRIMARY KEY,
    user1_id   BIGINT,
    user2_id   BIGINT,
    listing_id BIGINT
);

CREATE TABLE IF NOT EXISTS messages
(
    id         BIGSERIAL PRIMARY KEY,
    dialog_id  BIGINT,
    author_id  BIGINT,
    content    TEXT,
    created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS chat_images
(
    id         BIGSERIAL PRIMARY KEY,
    message_id BIGINT,
    url        VARCHAR(255)
);

--reviews
CREATE TABLE IF NOT EXISTS complaints
(
    id           BIGSERIAL PRIMARY KEY,
    listing_id   BIGINT,
    from_user_id BIGINT,
    to_user_id   BIGINT,
    comment      TEXT,
    is_reviewed  BOOLEAN DEFAULT FALSE,
    created_at   TIMESTAMP
);

CREATE TABLE IF NOT EXISTS complaint_images
(
    id           BIGSERIAL PRIMARY KEY,
    complaint_id BIGINT,
    url          VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS reviews
(
    id                BIGSERIAL PRIMARY KEY,
    listing_id        BIGINT,
    from_user_id      BIGINT,
    to_user_id        BIGINT,
    rating            INTEGER,
    comment           TEXT,
    moderation_status VARCHAR(32),
    created_at        TIMESTAMP
);

CREATE TABLE IF NOT EXISTS review_images
(
    id        BIGSERIAL PRIMARY KEY,
    review_id BIGINT,
    url       VARCHAR(255)
);


