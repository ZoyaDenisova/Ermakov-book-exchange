CREATE TABLE wanted_books
(
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    created_at TIMESTAMP,

    CONSTRAINT fk_wanted_books_user
      FOREIGN KEY (user_id)
          REFERENCES users(id)
          ON DELETE CASCADE,

    CONSTRAINT fk_wanted_books_book
      FOREIGN KEY (book_id)
          REFERENCES books(id)
          ON DELETE CASCADE,

    CONSTRAINT uc_user_book UNIQUE (user_id, book_id)
);