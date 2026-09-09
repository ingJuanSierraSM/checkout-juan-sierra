package com.ecommerce.core.checkout.infrastructure.web.dto;

import jakarta.validation.Valid;

import java.util.List;

public record QuoteCheckoutRequest(@Valid List<CheckoutItemRequest> items, String couponCode) {
}
