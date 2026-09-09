package com.ecommerce.core.shared.application.exception;

public class InactiveProductException extends BusinessException {

    public InactiveProductException(Long productId) {
        super(BusinessErrorCode.INACTIVE_PRODUCT, "El producto con id " + productId + " no está activo");
    }
}
