package com.ecommerce.core.catalog.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

public record Product(
        Long id,
        String name,
        BigDecimal unitPrice,
        Category category,
        Integer stock,
        boolean active,
        String imageUrl
) {

    public Product {
        Objects.requireNonNull(id, "Product id is required");
        Objects.requireNonNull(name, "Product name is required");
        Objects.requireNonNull(unitPrice, "Product unit price is required");
        Objects.requireNonNull(category, "Product category is required");
        Objects.requireNonNull(stock, "Product stock is required");

        if (unitPrice.signum() < 0) {
            throw new IllegalArgumentException("Product unit price cannot be negative");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("Product stock cannot be negative");
        }
    }
}
