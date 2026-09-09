package com.ecommerce.core.order.infrastructure.web.dto;

import com.ecommerce.core.catalog.domain.model.Category;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long productId,
        String productName,
        BigDecimal unitPrice,
        Category category,
        int quantity
) {
}
