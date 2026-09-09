package com.ecommerce.core.checkout.application.model;

import com.ecommerce.core.catalog.domain.model.Product;
import com.ecommerce.core.checkout.domain.model.Money;
import com.ecommerce.core.checkout.domain.model.PricingItem;

import java.util.Objects;

public record ResolvedCheckoutItem(Product product, int quantity) {

    public ResolvedCheckoutItem {
        Objects.requireNonNull(product, "Product is required");
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
    }

    public PricingItem toPricingItem() {
        return new PricingItem(
                product.id(),
                product.name(),
                Money.of(product.unitPrice()),
                product.category(),
                quantity
        );
    }
}
