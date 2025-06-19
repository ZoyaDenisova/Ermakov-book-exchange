ALTER TABLE messages
    ADD COLUMN exchange_id BIGINT,
ADD COLUMN is_exchange_proposal BOOLEAN DEFAULT FALSE;

ALTER TABLE messages
    ADD CONSTRAINT fk_messages_exchange
        FOREIGN KEY (exchange_id) REFERENCES exchanges(id)
            ON DELETE SET NULL;