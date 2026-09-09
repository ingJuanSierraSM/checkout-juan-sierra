package com.ecommerce.core.shared.application.exception;

public class EmptyCartException extends BusinessException {

    public EmptyCartException() {
        super(BusinessErrorCode.EMPTY_CART, "El carrito debe contener al menos un producto");
    }
}
