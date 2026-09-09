package com.ecommerce.core.checkout.application.model;

import java.util.List;

public record QuoteCheckoutCommand(List<CheckoutItem> items, String couponCode) {
}
