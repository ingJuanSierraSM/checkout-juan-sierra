package com.ecommerce.core.shared.application.exception;

public class InvalidCartException extends BusinessException {

    public InvalidCartException(String message) {
        super(BusinessErrorCode.INVALID_CART, message);
    }
}
