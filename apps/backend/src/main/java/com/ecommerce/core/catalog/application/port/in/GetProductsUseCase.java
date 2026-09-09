package com.ecommerce.core.catalog.application.port.in;

import com.ecommerce.core.catalog.domain.model.Product;

import java.util.List;

public interface GetProductsUseCase {

    List<Product> getActiveProducts();
}
