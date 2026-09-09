package com.ecommerce.core.promotion.application.port.out;

import com.ecommerce.core.promotion.domain.model.Coupon;

import java.util.Optional;

public interface CouponRepository {

    Optional<Coupon> findByCode(String code);

    Coupon save(Coupon coupon);
}
