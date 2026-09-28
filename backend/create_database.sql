-- AudioStore: chỉ tạo database rỗng.
-- Cấu trúc bảng được Flyway tạo từ src/main/resources/db/migration/V1..V7.
-- Không thêm CREATE TABLE tại đây để tránh xung đột với các migration.
CREATE DATABASE IF NOT EXISTS audiostore
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
