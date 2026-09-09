package com.ecommerce.core.checkout.domain.model;

import com.ecommerce.core.catalog.domain.model.Category;
import com.ecommerce.core.promotion.domain.model.Coupon;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record DiscountContext(
        List<PricingItem> items,
        Optional<Coupon> coupon,
        Instant now
) {

    public DiscountContext {
        Objects.requireNonNull(items, "Pricing items are required");
        Objects.requireNonNull(coupon, "Coupon container is required");
        Objects.requireNonNull(now, "Current time is required");
        items = List.copyOf(items);
    }

    public Money originalSubtotal() {
        return items.stream()
                .map(PricingItem::subtotal)
                .reduce(Money.zero(), Money::add);
    }

    public Money subtotalFor(Category category) {
        return items.stream()
                .filter(item -> item.category() == category)
                .map(PricingItem::subtotal)
                .reduce(Money.zero(), Money::add);
    }
}
