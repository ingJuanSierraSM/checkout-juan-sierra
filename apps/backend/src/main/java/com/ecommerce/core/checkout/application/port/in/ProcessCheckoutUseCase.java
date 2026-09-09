package com.ecommerce.core.checkout.application.port.in;

import com.ecommerce.core.checkout.application.model.ProcessedCheckout;
import com.ecommerce.core.checkout.application.model.QuoteCheckoutCommand;

public interface ProcessCheckoutUseCase {

    ProcessedCheckout process(QuoteCheckoutCommand command);
}
