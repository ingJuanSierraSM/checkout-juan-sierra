package com.ecommerce.core.promotion.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public record DiscountPolicy(
        Long id,
        String code,
        BigDecimal value,
        Instant updatedAt
) {

    public DiscountPolicy {
        Objects.requireNonNull(id, "Discount policy id is required");
        Objects.requireNonNull(code, "Discount policy code is required");
        Objects.requireNonNull(value, "Discount policy value is required");
        Objects.requireNonNull(updatedAt, "Discount policy update date is required");

        if (value.signum() < 0 || value.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Discount policy value must be between 0 and 100");
        }
    }
}
