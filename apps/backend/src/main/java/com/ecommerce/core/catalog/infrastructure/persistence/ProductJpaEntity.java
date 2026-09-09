package com.ecommerce.core.catalog.infrastructure.persistence;

import com.ecommerce.core.catalog.domain.model.Category;
import com.ecommerce.core.catalog.domain.model.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class ProductJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Category category;

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    protected ProductJpaEntity() {
    }

    public Product toDomain() {
        return new Product(id, name, unitPrice, category, stock, active, imageUrl);
    }
}
