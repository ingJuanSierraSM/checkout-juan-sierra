package com.ecommerce.core.shared.application.exception;

public class CouponExpiredException extends BusinessException {

    public CouponExpiredException(String couponCode) {
        super(BusinessErrorCode.COUPON_EXPIRED, "El cupón " + couponCode + " ha expirado");
    }
}
