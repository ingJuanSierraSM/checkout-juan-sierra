package com.ecommerce.core.order.infrastructure.web.dto;

import com.ecommerce.core.checkout.domain.model.DiscountType;

import java.math.BigDecimal;

public record OrderDiscountResponse(
        DiscountType type,
        String name,
        BigDecimal percentage,
        BigDecimal amount,
        int sequence
) {
}
