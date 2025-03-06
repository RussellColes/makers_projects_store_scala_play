# --- !Ups

CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    order_status VARCHAR(50) NOT NULL,
    ordered_at TIMESTAMP DEFAULT NOW(),
    total_amount NUMERIC(10,2) NOT NULL
);

CREATE TABLE order_items (
    id SERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    quantity BIGINT NOT NULL,
    unit_price BIGINT NOT NULL
);

ALTER TABLE orders ADD CONSTRAINT fk_user_orders FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;
ALTER TABLE order_items ADD CONSTRAINT fk_orders_order_items FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE;
ALTER TABLE order_items ADD CONSTRAINT fk_items_order_items FOREIGN KEY (item_id) REFERENCES items (id) ON DELETE CASCADE;

# --- !Downs

ALTER TABLE order_items DROP CONSTRAINT IF EXISTS fk_items_order_items;
ALTER TABLE order_items DROP CONSTRAINT IF EXISTS fk_orders_order_items;
ALTER TABLE orders DROP CONSTRAINT IF EXISTS fk_users;

DROP TABLE order_items;
DROP TABLE orders;