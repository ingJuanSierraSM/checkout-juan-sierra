package com.ecommerce.core.shared.application.exception;

public class CouponAlreadyUsedException extends BusinessException {

    public CouponAlreadyUsedException(String couponCode) {
        super(BusinessErrorCode.COUPON_ALREADY_USED, "El cupón " + couponCode + " ya fue utilizado");
    }
}
