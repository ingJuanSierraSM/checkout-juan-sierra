package com.ecommerce.core.shared.application.exception;

public class InsufficientStockException extends BusinessException {

    public InsufficientStockException(Long productId, int availableStock) {
        super(BusinessErrorCode.INSUFFICIENT_STOCK,
                "Stock insuficiente para el producto con id " + productId + ". Disponible: " + availableStock);
    }
}
