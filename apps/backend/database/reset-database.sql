BEGIN;

DELETE FROM order_discounts;
DELETE FROM order_items;
DELETE FROM orders;

ALTER SEQUENCE orders_id_seq RESTART WITH 1;
ALTER SEQUENCE order_items_id_seq RESTART WITH 1;
ALTER SEQUENCE order_discounts_id_seq RESTART WITH 1;

UPDATE products
SET stock = CASE id
    WHEN 1 THEN 5
    WHEN 2 THEN 6
    WHEN 3 THEN 10
    WHEN 4 THEN 8
    WHEN 5 THEN 7
    WHEN 6 THEN 12
    ELSE stock
END
WHERE id BETWEEN 1 AND 6;

UPDATE coupons
SET active = TRUE,
    used_at = NULL
WHERE code = 'WELCOME2026';

UPDATE discount_policies
SET value = 35.00,
    updated_at = CURRENT_TIMESTAMP
WHERE code = 'MAX_TOTAL_DISCOUNT_PERCENTAGE';

COMMIT;
