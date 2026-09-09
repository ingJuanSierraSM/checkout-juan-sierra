package com.ecommerce.core.checkout.application.port.in;

import com.ecommerce.core.checkout.application.model.CheckoutQuote;
import com.ecommerce.core.checkout.application.model.QuoteCheckoutCommand;

public interface QuoteCheckoutUseCase {

    CheckoutQuote quote(QuoteCheckoutCommand command);
}
