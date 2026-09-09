package com.ecommerce.core.checkout.infrastructure.web;

import com.ecommerce.core.checkout.application.port.in.QuoteCheckoutUseCase;
import com.ecommerce.core.checkout.infrastructure.web.dto.CheckoutQuoteResponse;
import com.ecommerce.core.checkout.infrastructure.web.dto.QuoteCheckoutRequest;
import com.ecommerce.core.checkout.infrastructure.web.mapper.CheckoutWebMapper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/checkout")
public class CheckoutController {

    private final QuoteCheckoutUseCase quoteCheckoutUseCase;
    private final CheckoutWebMapper checkoutWebMapper;

    public CheckoutController(QuoteCheckoutUseCase quoteCheckoutUseCase, CheckoutWebMapper checkoutWebMapper) {
        this.quoteCheckoutUseCase = quoteCheckoutUseCase;
        this.checkoutWebMapper = checkoutWebMapper;
    }

    @PostMapping("/quote")
    public CheckoutQuoteResponse quote(@Valid @RequestBody QuoteCheckoutRequest request) {
        return checkoutWebMapper.toQuoteResponse(
                quoteCheckoutUseCase.quote(checkoutWebMapper.toQuoteCommand(request))
        );
    }
}
