CREATE TABLE events (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(160) NOT NULL,
  starts_at DATETIME(6) NOT NULL,
  ends_at DATETIME(6) NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  PRIMARY KEY (id), INDEX ix_events_period (active, starts_at, ends_at)
);
CREATE TABLE promotions (
  id BIGINT NOT NULL AUTO_INCREMENT,
  event_id BIGINT NULL,
  name VARCHAR(160) NOT NULL,
  target_type VARCHAR(20) NOT NULL,
  target_id BIGINT NOT NULL,
  discount_percent DECIMAL(5,2) NOT NULL,
  starts_at DATETIME(6) NOT NULL,
  ends_at DATETIME(6) NOT NULL,
  flash_sale BOOLEAN NOT NULL DEFAULT FALSE,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  PRIMARY KEY (id),
  CONSTRAINT fk_promotions_event FOREIGN KEY (event_id) REFERENCES events(id),
  CONSTRAINT ck_promotions_target CHECK (target_type IN ('PRODUCT','CATEGORY')),
  CONSTRAINT ck_promotions_discount CHECK (discount_percent > 0 AND discount_percent <= 90),
  INDEX ix_promotions_active_period (active, starts_at, ends_at)
);
CREATE TABLE coupons (
  id BIGINT NOT NULL AUTO_INCREMENT,
  code VARCHAR(40) NOT NULL,
  discount_type VARCHAR(12) NOT NULL,
  discount_value DECIMAL(15,2) NOT NULL,
  minimum_order DECIMAL(15,2) NOT NULL DEFAULT 0,
  starts_at DATETIME(6) NOT NULL,
  ends_at DATETIME(6) NOT NULL,
  usage_limit INT NULL,
  used_count INT NOT NULL DEFAULT 0,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  PRIMARY KEY (id), UNIQUE KEY uk_coupons_code (code),
  CONSTRAINT ck_coupons_type CHECK (discount_type IN ('PERCENT','FIXED')),
  INDEX ix_coupons_active_period (active, starts_at, ends_at)
);
ALTER TABLE orders ADD COLUMN coupon_id BIGINT NULL AFTER address_id;
ALTER TABLE orders ADD CONSTRAINT fk_orders_coupon FOREIGN KEY (coupon_id) REFERENCES coupons(id);
