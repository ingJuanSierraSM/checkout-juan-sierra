package com.ecommerce.core.promotion.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataCouponRepository extends JpaRepository<CouponJpaEntity, Long> {

    Optional<CouponJpaEntity> findByCode(String code);
}
