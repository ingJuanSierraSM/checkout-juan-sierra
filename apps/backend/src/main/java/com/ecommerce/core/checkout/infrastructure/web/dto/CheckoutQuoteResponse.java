package com.ecommerce.core.checkout.infrastructure.web.dto;

import java.math.BigDecimal;
import java.util.List;

public record CheckoutQuoteResponse(
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
