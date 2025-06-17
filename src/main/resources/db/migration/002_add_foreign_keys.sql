--auth
ALTER TABLE users
    ADD CONSTRAINT fk_users_city
        FOREIGN KEY (city_id)
            REFERENCES cities (id)
            ON DELETE RESTRICT;

ALTER TABLE sessions
    ADD CONSTRAINT fk_sessions_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE RESTRICT;

--catalog
ALTER TABLE books
    ADD CONSTRAINT fk_books_user
        FOREIGN KEY (created_by)
            REFERENCES users (id)
            ON DELETE RESTRICT;

ALTER TABLE book_genres
    ADD CONSTRAINT fk_book_genres_book
        FOREIGN KEY (book_id)
            REFERENCES books (id)
            ON DELETE RESTRICT;

ALTER TABLE book_genres
    ADD CONSTRAINT fk_book_genres_genre
        FOREIGN KEY (genre_id)
            REFERENCES genres (id)
            ON DELETE RESTRICT;

ALTER TABLE book_images
    ADD CONSTRAINT fk_book_images_book
        FOREIGN KEY (book_id)
            REFERENCES books (id)
            ON DELETE RESTRICT;

--exchange
ALTER TABLE exchanges
    ADD CONSTRAINT fk_exchanges_sender
        FOREIGN KEY (sender_id)
            REFERENCES users (id)
            ON DELETE RESTRICT;

ALTER TABLE exchanges
    ADD CONSTRAINT fk_exchanges_receiver
        FOREIGN KEY (receiver_id)
            REFERENCES users (id)
            ON DELETE RESTRICT;

ALTER TABLE exchanges
    ADD CONSTRAINT fk_exchanges_offered
        FOREIGN KEY (offered_listing_id)
            REFERENCES listings (id)
            ON DELETE RESTRICT;

ALTER TABLE exchanges
    ADD CONSTRAINT fk_exchanges_selected
        FOREIGN KEY (selected_listing_id)
            REFERENCES listings (id)
            ON DELETE RESTRICT;

--listings
ALTER TABLE listings
    ADD CONSTRAINT fk_listings_owner
        FOREIGN KEY (owner_id)
            REFERENCES users (id)
            ON DELETE RESTRICT;

ALTER TABLE listings
    ADD CONSTRAINT fk_listings_book
        FOREIGN KEY (book_id)
            REFERENCES books (id)
            ON DELETE RESTRICT;

ALTER TABLE listings
    ADD CONSTRAINT fk_listings_city
        FOREIGN KEY (city_id)
            REFERENCES cities (id)
            ON DELETE RESTRICT;

ALTER TABLE listing_images
    ADD CONSTRAINT fk_listing_images_listing
        FOREIGN KEY (listing_id)
            REFERENCES listings (id)
            ON DELETE RESTRICT;

--messaging
ALTER TABLE dialogs
    ADD CONSTRAINT fk_dialogs_user1
        FOREIGN KEY (user1_id)
            REFERENCES users (id)
            ON DELETE RESTRICT;

ALTER TABLE dialogs
    ADD CONSTRAINT fk_dialogs_user2
        FOREIGN KEY (user2_id)
            REFERENCES users (id)
            ON DELETE RESTRICT;

ALTER TABLE dialogs
    ADD CONSTRAINT fk_dialogs_listing
        FOREIGN KEY (listing_id)
            REFERENCES listings (id)
            ON DELETE RESTRICT;

ALTER TABLE messages
    ADD CONSTRAINT fk_messages_dialog
        FOREIGN KEY (dialog_id)
            REFERENCES dialogs (id)
            ON DELETE RESTRICT;

ALTER TABLE messages
    ADD CONSTRAINT fk_messages_author
        FOREIGN KEY (author_id)
            REFERENCES users (id)
            ON DELETE RESTRICT;

ALTER TABLE chat_images
    ADD CONSTRAINT fk_chat_images_message
        FOREIGN KEY (message_id)
            REFERENCES messages (id)
            ON DELETE RESTRICT;

--reviews
ALTER TABLE complaints
    ADD CONSTRAINT fk_complaints_listing
        FOREIGN KEY (listing_id)
            REFERENCES listings (id)
            ON DELETE RESTRICT;

ALTER TABLE complaints
    ADD CONSTRAINT fk_complaints_from_user
        FOREIGN KEY (from_user_id)
            REFERENCES users (id)
            ON DELETE RESTRICT;

ALTER TABLE complaints
    ADD CONSTRAINT fk_complaints_to_user
        FOREIGN KEY (to_user_id)
            REFERENCES users (id)
            ON DELETE RESTRICT;

ALTER TABLE complaint_images
    ADD CONSTRAINT fk_complaint_images_complaint
        FOREIGN KEY (complaint_id)
            REFERENCES complaints (id)
            ON DELETE RESTRICT;

ALTER TABLE reviews
    ADD CONSTRAINT fk_reviews_listing
        FOREIGN KEY (listing_id)
            REFERENCES listings (id)
            ON DELETE RESTRICT;

ALTER TABLE reviews
    ADD CONSTRAINT fk_reviews_from_user
        FOREIGN KEY (from_user_id)
            REFERENCES users (id)
            ON DELETE RESTRICT;

ALTER TABLE reviews
    ADD CONSTRAINT fk_reviews_to_user
        FOREIGN KEY (to_user_id)
            REFERENCES users (id)
            ON DELETE RESTRICT;

ALTER TABLE review_images
    ADD CONSTRAINT fk_review_images_review
        FOREIGN KEY (review_id)
            REFERENCES reviews (id)
            ON DELETE RESTRICT;
