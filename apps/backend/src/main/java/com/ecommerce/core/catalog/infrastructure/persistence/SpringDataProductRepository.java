package com.ecommerce.core.catalog.infrastructure.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpringDataProductRepository extends JpaRepository<ProductJpaEntity, Long> {

    List<ProductJpaEntity> findByActiveTrueOrderByIdAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select product from ProductJpaEntity product where product.id = :id")
    Optional<ProductJpaEntity> findByIdForUpdate(@Param("id") Long id);
}
