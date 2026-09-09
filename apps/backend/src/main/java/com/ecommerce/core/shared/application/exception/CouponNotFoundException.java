package com.ecommerce.core.shared.application.exception;

public class CouponNotFoundException extends BusinessException {

    public CouponNotFoundException(String couponCode) {
        super(BusinessErrorCode.COUPON_NOT_FOUND, "No existe el cupón " + couponCode);
    }
}
