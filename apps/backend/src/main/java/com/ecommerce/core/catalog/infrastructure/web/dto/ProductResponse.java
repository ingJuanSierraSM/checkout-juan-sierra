package com.ecommerce.core.catalog.infrastructure.web.dto;

import com.ecommerce.core.catalog.domain.model.Category;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        BigDecimal unitPrice,
        Category category,
        Integer stock,
        String imageUrl
) {
}
