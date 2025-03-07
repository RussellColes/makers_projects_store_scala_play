# --- !Ups

CREATE TABLE carts (
    id SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    cart_status VARCHAR(50) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE cart_items (
    id SERIAL PRIMARY KEY,
    cart_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    quantity BIGINT NOT NULL
);

ALTER TABLE carts ADD CONSTRAINT fk_users FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;
ALTER TABLE cart_items ADD CONSTRAINT fk_carts FOREIGN KEY (cart_id) REFERENCES carts (id) ON DELETE CASCADE;
ALTER TABLE cart_items ADD CONSTRAINT fk_items FOREIGN KEY (item_id) REFERENCES items (id) ON DELETE CASCADE;

# --- !Downs

ALTER TABLE cart_items DROP CONSTRAINT IF EXISTS fk_items;
ALTER TABLE cart_items DROP CONSTRAINT IF EXISTS fk_carts;
ALTER TABLE carts DROP CONSTRAINT IF EXISTS fk_users;

DROP TABLE cart_items;
DROP TABLE carts;