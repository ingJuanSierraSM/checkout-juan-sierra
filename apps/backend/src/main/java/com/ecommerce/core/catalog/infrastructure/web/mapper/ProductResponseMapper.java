package com.ecommerce.core.catalog.infrastructure.web.mapper;

import com.ecommerce.core.catalog.domain.model.Product;
import com.ecommerce.core.catalog.infrastructure.web.dto.ProductResponse;
import org.springframework.stereotype.Component;

@Component
public class ProductResponseMapper {

    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.id(),
                product.name(),
                product.unitPrice(),
                product.category(),
                product.stock(),
                product.imageUrl()
        );
    }
}
