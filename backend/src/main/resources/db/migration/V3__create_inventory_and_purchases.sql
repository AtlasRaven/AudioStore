CREATE TABLE inventory (
  product_id BIGINT NOT NULL,
  quantity INT NOT NULL DEFAULT 0,
  minimum_quantity INT NOT NULL DEFAULT 0,
  version BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (product_id),
  CONSTRAINT fk_inventory_product FOREIGN KEY (product_id) REFERENCES products(id),
  CONSTRAINT ck_inventory_quantity CHECK (quantity >= 0),
  CONSTRAINT ck_inventory_minimum CHECK (minimum_quantity >= 0)
);
INSERT INTO inventory (product_id, quantity, minimum_quantity)
SELECT id, 0, 3 FROM products;
CREATE TABLE suppliers (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(180) NOT NULL,
  phone VARCHAR(30),
  email VARCHAR(190),
  address VARCHAR(500),
  active BOOLEAN NOT NULL DEFAULT TRUE,
  PRIMARY KEY (id)
);
CREATE TABLE purchases (
  id BIGINT NOT NULL AUTO_INCREMENT,
  supplier_id BIGINT NOT NULL,
  created_by BIGINT NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
  total DECIMAL(15,2) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  received_at TIMESTAMP NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_purchases_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id),
  CONSTRAINT fk_purchases_user FOREIGN KEY (created_by) REFERENCES users(id)
);
CREATE TABLE purchase_items (
  id BIGINT NOT NULL AUTO_INCREMENT,
  purchase_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  unit_cost DECIMAL(15,2) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_purchase_items_purchase FOREIGN KEY (purchase_id) REFERENCES purchases(id),
  CONSTRAINT fk_purchase_items_product FOREIGN KEY (product_id) REFERENCES products(id),
  CONSTRAINT ck_purchase_items_quantity CHECK (quantity > 0)
);
CREATE TABLE stock_movements (
  id BIGINT NOT NULL AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  actor_id BIGINT NOT NULL,
  type VARCHAR(24) NOT NULL,
  quantity_delta INT NOT NULL,
  reason VARCHAR(500) NOT NULL,
  reference_id BIGINT,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  CONSTRAINT fk_stock_movements_product FOREIGN KEY (product_id) REFERENCES products(id),
  CONSTRAINT fk_stock_movements_user FOREIGN KEY (actor_id) REFERENCES users(id),
  INDEX ix_stock_movements_product_date (product_id, created_at)
);
