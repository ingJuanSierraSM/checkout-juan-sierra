package com.ecommerce.core.promotion.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataDiscountPolicyRepository extends JpaRepository<DiscountPolicyJpaEntity, Long> {

    Optional<DiscountPolicyJpaEntity> findByCode(String code);
}
