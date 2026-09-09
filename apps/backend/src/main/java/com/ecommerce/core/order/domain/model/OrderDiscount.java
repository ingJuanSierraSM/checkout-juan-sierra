package com.ecommerce.core.order.domain.model;

import com.ecommerce.core.checkout.domain.model.DiscountType;
import com.ecommerce.core.checkout.domain.model.Money;

import java.math.BigDecimal;
import java.util.Objects;

public record OrderDiscount(
        DiscountType type,
        String name,
        BigDecimal percentage,
        Money amount,
        int sequence
) {

    public OrderDiscount {
        Objects.requireNonNull(type, "Discount type is required");
        Objects.requireNonNull(name, "Discount name is required");
        Objects.requireNonNull(percentage, "Discount percentage is required");
        Objects.requireNonNull(amount, "Discount amount is required");
        if (sequence <= 0) {
            throw new IllegalArgumentException("Discount sequence must be greater than zero");
        }
    }
}
