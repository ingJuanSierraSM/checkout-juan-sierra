package com.ecommerce.core.shared.application.exception;

public abstract class BusinessException extends RuntimeException {

    private final BusinessErrorCode errorCode;

    protected BusinessException(BusinessErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BusinessErrorCode errorCode() {
        return errorCode;
    }
}
