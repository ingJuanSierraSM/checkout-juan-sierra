package com.ecommerce.core.checkout.domain.model;

import com.ecommerce.core.catalog.domain.model.Category;

import java.util.Objects;

public record PricingItem(
        Long productId,
        String productName,
        Money unitPrice,
        Category category,
        int quantity
) {

    public PricingItem {
        Objects.requireNonNull(productId, "Product id is required");
        Objects.requireNonNull(productName, "Product name is required");
        Objects.requireNonNull(unitPrice, "Product unit price is required");
        Objects.requireNonNull(category, "Product category is required");
        if (quantity <= 0) {
            throw new IllegalArgumentException("Product quantity must be greater than zero");
        }
    }

    public Money subtotal() {
        return unitPrice.multiply(quantity);
    }
}
