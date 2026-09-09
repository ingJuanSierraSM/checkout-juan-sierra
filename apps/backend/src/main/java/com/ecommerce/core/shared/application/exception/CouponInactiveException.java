package com.ecommerce.core.shared.application.exception;

public class CouponInactiveException extends BusinessException {

    public CouponInactiveException(String couponCode) {
        super(BusinessErrorCode.COUPON_INACTIVE, "El cupón " + couponCode + " está inactivo");
    }
}
