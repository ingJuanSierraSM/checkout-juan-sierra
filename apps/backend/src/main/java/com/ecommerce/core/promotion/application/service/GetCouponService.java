package com.ecommerce.core.promotion.application.service;

import com.ecommerce.core.promotion.application.port.out.CouponRepository;
import com.ecommerce.core.promotion.domain.model.Coupon;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GetCouponService {

    private final CouponRepository couponRepository;

    public GetCouponService(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    public Optional<Coupon> findByCode(String code) {
        return couponRepository.findByCode(code);
    }
}
