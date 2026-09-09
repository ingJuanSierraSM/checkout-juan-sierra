package com.ecommerce.core.checkout.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

public record DiscountDetail(
        DiscountType type,
        String name,
        BigDecimal percentage,
        Money amount,
        int sequence
) {

    public DiscountDetail {
        Objects.requireNonNull(type, "Discount type is required");
        Objects.requireNonNull(name, "Discount name is required");
        Objects.requireNonNull(percentage, "Discount percentage is required");
        Objects.requireNonNull(amount, "Discount amount is required");
        if (percentage.signum() < 0 || percentage.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Discount percentage must be between 0 and 100");
        }
        if (amount.amount().signum() < 0) {
            throw new IllegalArgumentException("Discount amount cannot be negative");
        }
        if (sequence <= 0) {
            throw new IllegalArgumentException("Discount sequence must be greater than zero");
        }
    }
}
