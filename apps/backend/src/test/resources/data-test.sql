INSERT INTO products (id, name, unit_price, category, stock, active, image_url) VALUES
    (1, 'Laptop Pro', 120.00, 'TECHNOLOGY', 5, TRUE, '/products/laptop-pro.webp'),
    (2, 'Smartphone X', 90.00, 'TECHNOLOGY', 6, TRUE, '/products/smartphone-x.webp'),
    (3, 'Wireless Headphones', 40.00, 'TECHNOLOGY', 10, TRUE, '/products/wireless-headphones.webp'),
    (4, 'Office Chair', 80.00, 'HOME', 8, TRUE, '/products/office-chair.webp'),
    (5, 'Coffee Maker', 60.00, 'HOME', 7, TRUE, '/products/coffee-maker.webp'),
    (6, 'Urban Backpack', 35.00, 'ACCESSORIES', 12, TRUE, '/products/urban-backpack.webp');

INSERT INTO coupons (id, code, percentage, active, expires_at, used_at)
VALUES (1, 'WELCOME2026', 15.00, TRUE, TIMESTAMP WITH TIME ZONE '2027-12-31 23:59:59+00', NULL);

INSERT INTO discount_policies (id, code, value, updated_at)
VALUES (1, 'MAX_TOTAL_DISCOUNT_PERCENTAGE', 35.00, CURRENT_TIMESTAMP);
