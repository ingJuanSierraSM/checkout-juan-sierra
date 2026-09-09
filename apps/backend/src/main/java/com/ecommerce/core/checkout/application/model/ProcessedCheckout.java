package com.ecommerce.core.checkout.application.model;

import java.time.Instant;
import java.util.Objects;

public record ProcessedCheckout(
        Long orderId,
        Instant createdAt,
        CheckoutQuote quote
) {

    public ProcessedCheckout {
        Objects.requireNonNull(orderId, "Order id is required");
        Objects.requireNonNull(createdAt, "Creation date is required");
        Objects.requireNonNull(quote, "Checkout quote is required");
    }
}
