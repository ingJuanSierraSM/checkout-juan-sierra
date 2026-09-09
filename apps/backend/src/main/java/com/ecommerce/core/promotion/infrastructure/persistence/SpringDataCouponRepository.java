package com.ecommerce.core.promotion.infrastructure.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SpringDataCouponRepository extends JpaRepository<CouponJpaEntity, Long> {

    Optional<CouponJpaEntity> findByCode(String code);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select coupon from CouponJpaEntity coupon where coupon.code = :code")
    Optional<CouponJpaEntity> findByCodeForUpdate(@Param("code") String code);
}
