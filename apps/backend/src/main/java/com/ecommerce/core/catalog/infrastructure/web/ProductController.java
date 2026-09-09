package com.ecommerce.core.catalog.infrastructure.web;

import com.ecommerce.core.catalog.application.port.in.GetProductsUseCase;
import com.ecommerce.core.catalog.infrastructure.web.dto.ProductResponse;
import com.ecommerce.core.catalog.infrastructure.web.mapper.ProductResponseMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final GetProductsUseCase getProductsUseCase;
    private final ProductResponseMapper productResponseMapper;

    public ProductController(
            GetProductsUseCase getProductsUseCase,
            ProductResponseMapper productResponseMapper
    ) {
        this.getProductsUseCase = getProductsUseCase;
        this.productResponseMapper = productResponseMapper;
    }

    @GetMapping
    public List<ProductResponse> getProducts() {
        return getProductsUseCase.getActiveProducts()
                .stream()
                .map(productResponseMapper::toResponse)
                .toList();
    }
}
