package com.ecommerce.core.checkout.application.model;

import com.ecommerce.core.checkout.domain.model.DiscountBreakdown;

import java.util.Objects;

public record CheckoutQuote(DiscountBreakdown breakdown) {

    public CheckoutQuote {
        Objects.requireNonNull(breakdown, "Discount breakdown is required");
    }
}
