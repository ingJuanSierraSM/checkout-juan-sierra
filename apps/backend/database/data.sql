INSERT INTO products (id, name, unit_price, category, stock, active, image_url)
VALUES
    (1, 'Laptop Pro', 120.00, 'TECHNOLOGY', 5, TRUE, '/products/laptop-pro.webp'),
    (2, 'Smartphone X', 90.00, 'TECHNOLOGY', 6, TRUE, '/products/smartphone-x.webp'),
    (3, 'Wireless Headphones', 40.00, 'TECHNOLOGY', 10, TRUE, '/products/wireless-headphones.webp'),
    (4, 'Office Chair', 80.00, 'HOME', 8, TRUE, '/products/office-chair.webp'),
    (5, 'Coffee Maker', 60.00, 'HOME', 7, TRUE, '/products/coffee-maker.webp'),
    (6, 'Urban Backpack', 35.00, 'ACCESSORIES', 12, TRUE, '/products/urban-backpack.webp')
ON CONFLICT (id) DO NOTHING;

INSERT INTO coupons (code, percentage, active, expires_at, used_at)
VALUES ('WELCOME2026', 15.00, TRUE, '2027-12-31 23:59:59+00', NULL)
ON CONFLICT (code) DO NOTHING;

INSERT INTO discount_policies (code, value)
VALUES ('MAX_TOTAL_DISCOUNT_PERCENTAGE', 35.00)
ON CONFLICT (code) DO NOTHING;

SELECT setval(
    pg_get_serial_sequence('products', 'id'),
    (SELECT COALESCE(MAX(id), 1) FROM products),
    TRUE
);
