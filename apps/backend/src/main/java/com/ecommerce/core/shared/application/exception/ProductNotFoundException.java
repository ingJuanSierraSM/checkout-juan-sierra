package com.ecommerce.core.shared.application.exception;

public class ProductNotFoundException extends BusinessException {

    public ProductNotFoundException(Long productId) {
        super(BusinessErrorCode.PRODUCT_NOT_FOUND, "No existe el producto con id " + productId);
    }
}
