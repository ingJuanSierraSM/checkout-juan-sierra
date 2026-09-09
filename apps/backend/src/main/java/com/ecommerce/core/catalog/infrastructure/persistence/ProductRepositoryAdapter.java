package com.ecommerce.core.catalog.infrastructure.persistence;

import com.ecommerce.core.catalog.application.port.out.ProductRepository;
import com.ecommerce.core.catalog.domain.model.Product;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductRepositoryAdapter implements ProductRepository {

    private final SpringDataProductRepository repository;

    public ProductRepositoryAdapter(SpringDataProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Product> findActiveProducts() {
        return repository.findByActiveTrueOrderByIdAsc()
                .stream()
                .map(ProductJpaEntity::toDomain)
                .toList();
    }
}
