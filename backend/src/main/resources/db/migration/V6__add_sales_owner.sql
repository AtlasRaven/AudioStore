ALTER TABLE orders ADD COLUMN sales_owner_id BIGINT NULL AFTER user_id;
ALTER TABLE orders ADD CONSTRAINT fk_orders_sales_owner FOREIGN KEY (sales_owner_id) REFERENCES users(id);
CREATE INDEX ix_orders_sales_owner_date ON orders (sales_owner_id, status, created_at);
