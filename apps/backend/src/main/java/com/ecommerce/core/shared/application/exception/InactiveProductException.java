package com.ecommerce.core.shared.application.exception;

public class InactiveProductException extends BusinessException {

    public InactiveProductException(Long productId) {
        super(BusinessErrorCode.PRODUCT_INACTIVE, "El producto con id " + productId + " no está activo");
    }
}
