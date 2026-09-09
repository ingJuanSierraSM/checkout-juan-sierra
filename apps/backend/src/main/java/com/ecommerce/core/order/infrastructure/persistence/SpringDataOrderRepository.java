package com.ecommerce.core.order.infrastructure.persistence;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SpringDataOrderRepository extends JpaRepository<OrderJpaEntity, Long> {

    @EntityGraph(attributePaths = {"items", "discounts"})
    List<OrderJpaEntity> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"items", "discounts"})
    @Query("select order from OrderJpaEntity order where order.id = :id")
    Optional<OrderJpaEntity> findDetailedById(Long id);
}
