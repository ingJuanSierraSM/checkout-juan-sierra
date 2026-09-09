package com.ecommerce.core.checkout.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

public record DiscountBreakdown(
        Money originalSubtotal,
        List<DiscountDetail> discounts,
        Money calculatedDiscountBeforeCap,
        Money totalDiscount,
        BigDecimal effectiveDiscountPercentage,
        Money finalTotal,
        boolean discountCapApplied,
        BigDecimal maximumDiscountPercentage
) {

    public DiscountBreakdown {
        Objects.requireNonNull(originalSubtotal, "Original subtotal is required");
        Objects.requireNonNull(discounts, "Discount details are required");
        Objects.requireNonNull(calculatedDiscountBeforeCap, "Calculated discount is required");
        Objects.requireNonNull(totalDiscount, "Total discount is required");
        Objects.requireNonNull(effectiveDiscountPercentage, "Effective discount percentage is required");
        Objects.requireNonNull(finalTotal, "Final total is required");
        Objects.requireNonNull(maximumDiscountPercentage, "Maximum discount percentage is required");
        discounts = List.copyOf(discounts);
        effectiveDiscountPercentage = effectiveDiscountPercentage.setScale(2, RoundingMode.HALF_UP);
        maximumDiscountPercentage = maximumDiscountPercentage.setScale(2, RoundingMode.HALF_UP);
    }
}
