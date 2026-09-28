CREATE TABLE categories (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  slug VARCHAR(120) NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  PRIMARY KEY (id), UNIQUE KEY uk_categories_name (name), UNIQUE KEY uk_categories_slug (slug)
);
CREATE TABLE brands (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  slug VARCHAR(120) NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  PRIMARY KEY (id), UNIQUE KEY uk_brands_name (name), UNIQUE KEY uk_brands_slug (slug)
);
CREATE TABLE products (
  id BIGINT NOT NULL AUTO_INCREMENT,
  sku VARCHAR(40) NOT NULL,
  name VARCHAR(180) NOT NULL,
  slug VARCHAR(200) NOT NULL,
  category_id BIGINT NOT NULL,
  brand_id BIGINT NOT NULL,
  description TEXT,
  specifications TEXT,
  sale_price DECIMAL(15,2) NOT NULL,
  cost_price DECIMAL(15,2) NOT NULL,
  image_url VARCHAR(1000),
  warranty_months INT NOT NULL DEFAULT 0,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  PRIMARY KEY (id),
  UNIQUE KEY uk_products_sku (sku), UNIQUE KEY uk_products_slug (slug),
  CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories(id),
  CONSTRAINT fk_products_brand FOREIGN KEY (brand_id) REFERENCES brands(id),
  INDEX ix_products_category (category_id), INDEX ix_products_brand (brand_id), INDEX ix_products_price (sale_price)
);
