package com.ecommerce.core.catalog.application.service;

import com.ecommerce.core.catalog.application.port.out.ProductRepository;
import com.ecommerce.core.catalog.domain.model.Category;
import com.ecommerce.core.catalog.domain.model.Product;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

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
        ProductRepository repository = new ProductRepository() {
            @Override
            public List<Product> findActiveProducts() {
                return List.of(product);
            }

            @Override
            public Optional<Product> findById(Long id) {
                return Optional.of(product);
            }

            @Override
            public Optional<Product> findByIdForUpdate(Long id) {
                return Optional.of(product);
            }

            @Override
            public Product save(Product productToSave) {
                return productToSave;
            }
        };
        GetProductsService service = new GetProductsService(repository);

        List<Product> result = service.getActiveProducts();

        assertThat(result).containsExactly(product);
    }
}
