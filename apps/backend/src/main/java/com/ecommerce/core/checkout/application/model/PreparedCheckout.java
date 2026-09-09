package com.ecommerce.core.checkout.application.model;

import com.ecommerce.core.checkout.domain.model.DiscountBreakdown;
import com.ecommerce.core.promotion.domain.model.Coupon;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record PreparedCheckout(
        List<ResolvedCheckoutItem> items,
        Optional<Coupon> coupon,
        DiscountBreakdown breakdown,
        Instant calculatedAt
) {

    public PreparedCheckout {
        items = List.copyOf(items);
        coupon = Objects.requireNonNull(coupon, "Coupon is required");
        Objects.requireNonNull(breakdown, "Discount breakdown is required");
        Objects.requireNonNull(calculatedAt, "Calculation date is required");
    }
}
