package com.ecommerce.core.checkout.infrastructure.web.dto;

import com.ecommerce.core.checkout.domain.model.DiscountType;

import java.math.BigDecimal;

public record DiscountDetailResponse(
        DiscountType type,
        String name,
        BigDecimal percentage,
        BigDecimal amount,
        int sequence
) {
}
