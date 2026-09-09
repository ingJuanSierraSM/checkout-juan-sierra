package com.ecommerce.core.order.domain.model;

import com.ecommerce.core.catalog.domain.model.Category;
import com.ecommerce.core.checkout.domain.model.Money;

import java.util.Objects;

public record OrderItem(
        Long productId,
        String productName,
        Money unitPrice,
        Category category,
        int quantity
) {

    public OrderItem {
        Objects.requireNonNull(productId, "Product id is required");
        Objects.requireNonNull(productName, "Product name is required");
        Objects.requireNonNull(unitPrice, "Unit price is required");
        Objects.requireNonNull(category, "Category is required");
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
    }
}
