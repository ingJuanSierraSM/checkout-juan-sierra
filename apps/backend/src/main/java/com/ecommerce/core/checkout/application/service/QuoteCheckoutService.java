package com.ecommerce.core.checkout.application.service;

import com.ecommerce.core.checkout.application.model.CheckoutQuote;
import com.ecommerce.core.checkout.application.model.QuoteCheckoutCommand;
import com.ecommerce.core.checkout.application.port.in.QuoteCheckoutUseCase;
import org.springframework.stereotype.Service;

@Service
public class QuoteCheckoutService implements QuoteCheckoutUseCase {

    private final CheckoutPricingService checkoutPricingService;

    public QuoteCheckoutService(CheckoutPricingService checkoutPricingService) {
        this.checkoutPricingService = checkoutPricingService;
    }

    @Override
    public CheckoutQuote quote(QuoteCheckoutCommand command) {
        return new CheckoutQuote(checkoutPricingService.prepareQuote(command).breakdown());
    }
}
