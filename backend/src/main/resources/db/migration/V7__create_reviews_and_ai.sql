CREATE TABLE reviews (
  id BIGINT NOT NULL AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  order_id BIGINT NOT NULL,
  rating INT NOT NULL,
  content VARCHAR(2000) NOT NULL,
  visible BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  CONSTRAINT uk_review_order_product UNIQUE (order_id, product_id),
  CONSTRAINT fk_reviews_product FOREIGN KEY (product_id) REFERENCES products(id),
  CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_reviews_order FOREIGN KEY (order_id) REFERENCES orders(id),
  CONSTRAINT ck_reviews_rating CHECK (rating BETWEEN 1 AND 5),
  INDEX ix_reviews_product_visible (product_id, visible, created_at)
);
CREATE TABLE ai_review_summaries (
  product_id BIGINT NOT NULL,
  summary TEXT NOT NULL,
  review_count BIGINT NOT NULL,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (product_id),
  CONSTRAINT fk_ai_review_summaries_product FOREIGN KEY (product_id) REFERENCES products(id)
);
CREATE TABLE ai_logs (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NULL,
  feature VARCHAR(40) NOT NULL,
  status VARCHAR(20) NOT NULL,
  duration_ms INT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  CONSTRAINT fk_ai_logs_user FOREIGN KEY (user_id) REFERENCES users(id),
  INDEX ix_ai_logs_created (created_at)
);
