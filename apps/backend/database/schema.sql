CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    unit_price NUMERIC(12, 2) NOT NULL CHECK (unit_price >= 0),
    category VARCHAR(50) NOT NULL CHECK (category IN ('TECHNOLOGY', 'HOME', 'ACCESSORIES')),
    stock INTEGER NOT NULL CHECK (stock >= 0),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    image_url VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS coupons (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(80) NOT NULL UNIQUE,
    percentage NUMERIC(7, 2) NOT NULL CHECK (percentage >= 0 AND percentage <= 100),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    expires_at TIMESTAMPTZ NOT NULL,
    used_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS discount_policies (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    value NUMERIC(7, 2) NOT NULL CHECK (value >= 0 AND value <= 100),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    original_subtotal NUMERIC(12, 2) NOT NULL CHECK (original_subtotal >= 0),
    calculated_discount_before_cap NUMERIC(12, 2) NOT NULL CHECK (calculated_discount_before_cap >= 0),
    total_discount NUMERIC(12, 2) NOT NULL CHECK (total_discount >= 0),
    effective_discount_percentage NUMERIC(7, 2) NOT NULL CHECK (effective_discount_percentage >= 0),
    final_total NUMERIC(12, 2) NOT NULL CHECK (final_total >= 0),
    discount_cap_applied BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id),
    product_name VARCHAR(150) NOT NULL,
    unit_price NUMERIC(12, 2) NOT NULL CHECK (unit_price >= 0),
    category VARCHAR(50) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0)
);

CREATE TABLE IF NOT EXISTS order_discounts (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    type VARCHAR(30) NOT NULL,
    name VARCHAR(150) NOT NULL,
    percentage NUMERIC(7, 2) NOT NULL CHECK (percentage >= 0),
    amount NUMERIC(12, 2) NOT NULL CHECK (amount >= 0),
    sequence INTEGER NOT NULL CHECK (sequence > 0),
    CONSTRAINT uq_order_discounts_order_sequence UNIQUE (order_id, sequence)
);

CREATE INDEX IF NOT EXISTS idx_orders_created_at
    ON orders (created_at DESC);

CREATE INDEX IF NOT EXISTS idx_order_items_order_id
    ON order_items (order_id);

CREATE INDEX IF NOT EXISTS idx_order_discounts_order_id
    ON order_discounts (order_id);
