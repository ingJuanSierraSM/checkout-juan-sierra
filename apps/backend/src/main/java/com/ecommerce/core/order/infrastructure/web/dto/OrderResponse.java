package com.ecommerce.core.order.infrastructure.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        BigDecimal originalSubtotal,
        BigDecimal calculatedDiscountBeforeCap,
        BigDecimal totalDiscount,
        BigDecimal effectiveDiscountPercentage,
        BigDecimal finalTotal,
        boolean discountCapApplied,
        Instant createdAt,
        List<OrderItemResponse> items,
        List<OrderDiscountResponse> discounts
) {
}
