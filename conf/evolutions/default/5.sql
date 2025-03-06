# --- !Ups

CREATE TABLE payments (
    id SERIAL PRIMARY KEY,
    amount NUMERIC(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL CHECK (char_length(currency) = 3),
    status VARCHAR(20) NOT NULL CHECK (status IN ('pending', 'processed', 'failed')),
    user_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    completed_at TIMESTAMP
);

ALTER TABLE payments ADD CONSTRAINT fk_users FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;
ALTER TABLE payments ADD CONSTRAINT fk_orders FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE;

# --- !Downs

ALTER TABLE payments DROP CONSTRAINT IF EXISTS fk_orders;
ALTER TABLE payments DROP CONSTRAINT IF EXISTS fk_users;

DROP TABLE payments;