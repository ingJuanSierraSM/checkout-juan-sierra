package com.ecommerce.core.order.domain.model;

import com.ecommerce.core.checkout.domain.model.DiscountBreakdown;
import com.ecommerce.core.checkout.domain.model.Money;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

public record Order(
        Long id,
        Money originalSubtotal,
        Money calculatedDiscountBeforeCap,
        Money totalDiscount,
        BigDecimal effectiveDiscountPercentage,
        Money finalTotal,
        boolean discountCapApplied,
        Instant createdAt,
        List<OrderItem> items,
        List<OrderDiscount> discounts
) {

    public Order {
        Objects.requireNonNull(originalSubtotal, "Original subtotal is required");
        Objects.requireNonNull(calculatedDiscountBeforeCap, "Calculated discount is required");
        Objects.requireNonNull(totalDiscount, "Total discount is required");
        Objects.requireNonNull(effectiveDiscountPercentage, "Effective discount percentage is required");
        Objects.requireNonNull(finalTotal, "Final total is required");
        Objects.requireNonNull(createdAt, "Creation date is required");
        items = List.copyOf(items);
        discounts = List.copyOf(discounts);
        if (items.isEmpty()) {
            throw new IllegalArgumentException("An order must contain at least one item");
        }
    }

    public static Order create(
            DiscountBreakdown breakdown,
            Instant createdAt,
            List<OrderItem> items,
            List<OrderDiscount> discounts
    ) {
        return new Order(
                null,
                breakdown.originalSubtotal(),
                breakdown.calculatedDiscountBeforeCap(),
                breakdown.totalDiscount(),
                breakdown.effectiveDiscountPercentage(),
                breakdown.finalTotal(),
                breakdown.discountCapApplied(),
                createdAt,
                items,
                discounts
        );
    }
}
