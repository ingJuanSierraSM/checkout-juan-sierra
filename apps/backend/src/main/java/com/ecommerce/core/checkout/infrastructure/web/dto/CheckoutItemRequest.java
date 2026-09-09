package com.ecommerce.core.checkout.infrastructure.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CheckoutItemRequest(
        @NotNull(message = "El id del producto es obligatorio")
        @Positive(message = "El id del producto debe ser mayor que cero") Long productId,
        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor que cero") Integer quantity
) {
}
