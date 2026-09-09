package com.ecommerce.core.promotion.application.port.out;

import com.ecommerce.core.promotion.domain.model.Coupon;

import java.util.Optional;

public interface CouponRepository {

    Optional<Coupon> findByCode(String code);

    Optional<Coupon> findByCodeForUpdate(String code);

    Coupon save(Coupon coupon);
}
