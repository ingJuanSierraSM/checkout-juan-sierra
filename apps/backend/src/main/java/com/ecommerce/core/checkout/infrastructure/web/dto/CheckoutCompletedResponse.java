package com.ecommerce.core.checkout.infrastructure.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record CheckoutCompletedResponse(
        Long orderId,
        Instant createdAt,
        BigDecimal originalSubtotal,
        List<DiscountDetailResponse> discounts,
        BigDecimal calculatedDiscountBeforeCap,
        BigDecimal totalDiscount,
        BigDecimal effectiveDiscountPercentage,
        BigDecimal finalTotal,
        boolean discountCapApplied,
        BigDecimal maximumDiscountPercentage
) {
}
