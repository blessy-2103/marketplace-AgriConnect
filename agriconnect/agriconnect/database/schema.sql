-- ==========================================================
-- AgriConnect - Reference schema
-- Hibernate (spring.jpa.hibernate.ddl-auto=update) will create/
-- update these tables automatically on startup. This file is
-- provided for reference, manual setup, or if you prefer to
-- manage the schema yourself (set ddl-auto=validate or none).
-- ==========================================================

CREATE DATABASE IF NOT EXISTS agriconnect_db;
USE agriconnect_db;

CREATE TABLE IF NOT EXISTS users (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    phone           VARCHAR(50),
    address         VARCHAR(500),
    role            VARCHAR(20) NOT NULL,
    farm_name       VARCHAR(255),
    farm_location   VARCHAR(255),
    bio             TEXT,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS categories (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100) NOT NULL UNIQUE,
    description     VARCHAR(500),
    icon            VARCHAR(10)
);

CREATE TABLE IF NOT EXISTS products (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    name                VARCHAR(255) NOT NULL,
    description         TEXT,
    price               DECIMAL(10,2) NOT NULL,
    unit                VARCHAR(50) NOT NULL,
    quantity_available  INT NOT NULL,
    image_url           VARCHAR(500),
    organic             BOOLEAN NOT NULL DEFAULT FALSE,
    active              BOOLEAN NOT NULL DEFAULT TRUE,
    category_id         BIGINT,
    farmer_id           BIGINT NOT NULL,
    created_at          DATETIME NOT NULL,
    updated_at          DATETIME,
    FOREIGN KEY (category_id) REFERENCES categories(id),
    FOREIGN KEY (farmer_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS orders (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id         BIGINT NOT NULL,
    total_amount        DECIMAL(10,2) NOT NULL,
    delivery_address    VARCHAR(500) NOT NULL,
    contact_phone       VARCHAR(50),
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    order_date          DATETIME NOT NULL,
    updated_at          DATETIME,
    FOREIGN KEY (customer_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS order_items (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id            BIGINT NOT NULL,
    product_id          BIGINT NOT NULL,
    farmer_id           BIGINT NOT NULL,
    quantity            INT NOT NULL,
    price_at_order      DECIMAL(10,2) NOT NULL,
    subtotal            DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (farmer_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS auth_tokens (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    token           VARCHAR(100) NOT NULL UNIQUE,
    user_id         BIGINT NOT NULL,
    expires_at      DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Seed categories
INSERT IGNORE INTO categories (name, description, icon) VALUES
('Vegetables', 'Fresh, farm-picked vegetables', '🥬'),
('Fruits', 'Seasonal fruits straight from the orchard', '🍎'),
('Grains & Pulses', 'Rice, wheat, lentils and more', '🌾'),
('Dairy', 'Milk, cheese, curd and ghee', '🥛'),
('Herbs & Spices', 'Fresh herbs and homegrown spices', '🌿'),
('Honey & Preserves', 'Raw honey, jams and pickles', '🍯'),
('Poultry & Eggs', 'Free-range eggs and poultry', '🥚'),
('Flowers & Plants', 'Cut flowers, saplings and seeds', '🌻');
