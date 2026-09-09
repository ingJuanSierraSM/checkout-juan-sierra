package com.ecommerce.core.catalog.application.service;

import com.ecommerce.core.catalog.application.port.out.ProductRepository;
import com.ecommerce.core.catalog.domain.model.Category;
import com.ecommerce.core.catalog.domain.model.Product;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GetProductsServiceTest {

    @Test
    void shouldReturnProductsProvidedByTheRepository() {
        Product product = new Product(
                1L,
                "Laptop Pro",
                new BigDecimal("120.00"),
                Category.TECHNOLOGY,
                5,
                true,
                "/products/laptop-pro.webp"
        );
        GetProductsService service = new GetProductsService(() -> List.of(product));

        List<Product> result = service.getActiveProducts();

        assertThat(result).containsExactly(product);
    }
}
