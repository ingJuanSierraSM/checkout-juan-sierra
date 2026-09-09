package com.ecommerce.core.promotion.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public record Coupon(
        Long id,
        String code,
        BigDecimal percentage,
        boolean active,
        Instant expiresAt,
        Instant usedAt
) {

    public Coupon {
        Objects.requireNonNull(id, "Coupon id is required");
        Objects.requireNonNull(code, "Coupon code is required");
        Objects.requireNonNull(percentage, "Coupon percentage is required");
        Objects.requireNonNull(expiresAt, "Coupon expiration date is required");

        if (percentage.signum() < 0 || percentage.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Coupon percentage must be between 0 and 100");
        }
    }

    public boolean isUsableAt(Instant now) {
        Objects.requireNonNull(now, "Current time is required");
        return active && usedAt == null && expiresAt.isAfter(now);
    }

    public Coupon markAsUsed(Instant usedAt) {
        return new Coupon(id, code, percentage, active, expiresAt,
                Objects.requireNonNull(usedAt, "Used date is required"));
    }
}
