package com.ecommerce.core.catalog.application.service;

import com.ecommerce.core.catalog.application.port.in.GetProductsUseCase;
import com.ecommerce.core.catalog.application.port.out.ProductRepository;
import com.ecommerce.core.catalog.domain.model.Product;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetProductsService implements GetProductsUseCase {

    private final ProductRepository productRepository;

    public GetProductsService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public List<Product> getActiveProducts() {
        return productRepository.findActiveProducts();
    }
}
