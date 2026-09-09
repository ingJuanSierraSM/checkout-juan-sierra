package com.ecommerce.core.shared.application.exception;

public class OrderNotFoundException extends BusinessException {

    public OrderNotFoundException(Long orderId) {
        super(BusinessErrorCode.ORDER_NOT_FOUND, "No existe la orden con id " + orderId);
    }
}
