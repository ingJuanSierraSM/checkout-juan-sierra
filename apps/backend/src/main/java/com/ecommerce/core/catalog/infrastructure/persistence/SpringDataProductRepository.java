package com.ecommerce.core.catalog.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataProductRepository extends JpaRepository<ProductJpaEntity, Long> {

    List<ProductJpaEntity> findByActiveTrueOrderByIdAsc();
}
