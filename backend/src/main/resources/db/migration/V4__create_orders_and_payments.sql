ALTER TABLE inventory ADD COLUMN reserved_quantity INT NOT NULL DEFAULT 0 AFTER quantity;
ALTER TABLE inventory ADD CONSTRAINT ck_inventory_reserved CHECK (reserved_quantity >= 0 AND reserved_quantity <= quantity);
CREATE TABLE addresses (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  recipient VARCHAR(120) NOT NULL,
  phone VARCHAR(30) NOT NULL,
  province VARCHAR(100) NOT NULL,
  district VARCHAR(100) NOT NULL,
  detail VARCHAR(500) NOT NULL,
  default_address BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (id),
  CONSTRAINT fk_addresses_user FOREIGN KEY (user_id) REFERENCES users(id),
  INDEX ix_addresses_user_default (user_id, default_address)
);
CREATE TABLE orders (
  id BIGINT NOT NULL AUTO_INCREMENT,
  code VARCHAR(40) NOT NULL,
  user_id BIGINT NOT NULL,
  address_id BIGINT NOT NULL,
  recipient VARCHAR(120) NOT NULL,
  phone VARCHAR(30) NOT NULL,
  shipping_address VARCHAR(800) NOT NULL,
  status VARCHAR(30) NOT NULL,
  payment_method VARCHAR(20) NOT NULL,
  subtotal DECIMAL(15,2) NOT NULL,
  discount DECIMAL(15,2) NOT NULL DEFAULT 0,
  total DECIMAL(15,2) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id), UNIQUE KEY uk_orders_code (code),
  CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_orders_address FOREIGN KEY (address_id) REFERENCES addresses(id),
  INDEX ix_orders_status_created (status, created_at), INDEX ix_orders_user_created (user_id, created_at)
);
CREATE TABLE order_items (
  id BIGINT NOT NULL AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  product_name VARCHAR(180) NOT NULL,
  quantity INT NOT NULL,
  unit_price DECIMAL(15,2) NOT NULL,
  cost_price DECIMAL(15,2) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id),
  CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products(id),
  CONSTRAINT ck_order_items_quantity CHECK (quantity > 0)
);
CREATE TABLE payments (
  id BIGINT NOT NULL AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  status VARCHAR(20) NOT NULL,
  method VARCHAR(20) NOT NULL,
  amount DECIMAL(15,2) NOT NULL,
  confirmed_at TIMESTAMP NULL,
  confirmed_by BIGINT NULL,
  PRIMARY KEY (id), UNIQUE KEY uk_payments_order (order_id),
  CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES orders(id),
  CONSTRAINT fk_payments_confirmed_by FOREIGN KEY (confirmed_by) REFERENCES users(id)
);
CREATE TABLE shipments (
  id BIGINT NOT NULL AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  status VARCHAR(20) NOT NULL,
  shipped_at TIMESTAMP NOT NULL,
  note VARCHAR(500),
  PRIMARY KEY (id), UNIQUE KEY uk_shipments_order (order_id),
  CONSTRAINT fk_shipments_order FOREIGN KEY (order_id) REFERENCES orders(id)
);
