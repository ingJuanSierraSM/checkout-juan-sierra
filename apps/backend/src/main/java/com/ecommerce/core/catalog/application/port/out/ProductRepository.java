package com.ecommerce.core.catalog.application.port.out;

import com.ecommerce.core.catalog.domain.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    List<Product> findActiveProducts();

    Optional<Product> findById(Long id);
}
