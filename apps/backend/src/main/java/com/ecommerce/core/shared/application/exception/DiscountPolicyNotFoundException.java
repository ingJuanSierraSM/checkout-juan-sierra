package com.ecommerce.core.shared.application.exception;

public class DiscountPolicyNotFoundException extends BusinessException {

    public DiscountPolicyNotFoundException(String policyCode) {
        super(BusinessErrorCode.DISCOUNT_POLICY_NOT_FOUND, "No existe la política " + policyCode);
    }
}
