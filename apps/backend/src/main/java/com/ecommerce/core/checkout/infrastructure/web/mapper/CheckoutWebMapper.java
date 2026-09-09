package com.ecommerce.core.checkout.infrastructure.web.mapper;

import com.ecommerce.core.checkout.application.model.CheckoutItem;
import com.ecommerce.core.checkout.application.model.CheckoutQuote;
import com.ecommerce.core.checkout.application.model.QuoteCheckoutCommand;
import com.ecommerce.core.checkout.domain.model.DiscountDetail;
import com.ecommerce.core.checkout.infrastructure.web.dto.CheckoutQuoteResponse;
import com.ecommerce.core.checkout.infrastructure.web.dto.DiscountDetailResponse;
import com.ecommerce.core.checkout.infrastructure.web.dto.QuoteCheckoutRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CheckoutWebMapper {

    public QuoteCheckoutCommand toQuoteCommand(QuoteCheckoutRequest request) {
        List<CheckoutItem> items = request.items() == null
                ? null
                : request.items().stream()
                .map(item -> new CheckoutItem(item.productId(), item.quantity()))
                .toList();
        return new QuoteCheckoutCommand(items, request.couponCode());
    }

    public CheckoutQuoteResponse toQuoteResponse(CheckoutQuote quote) {
        var breakdown = quote.breakdown();
        return new CheckoutQuoteResponse(
                breakdown.originalSubtotal().amount(),
                breakdown.discounts().stream().map(this::toDiscountDetailResponse).toList(),
                breakdown.calculatedDiscountBeforeCap().amount(),
                breakdown.totalDiscount().amount(),
                breakdown.effectiveDiscountPercentage(),
                breakdown.finalTotal().amount(),
                breakdown.discountCapApplied(),
                breakdown.maximumDiscountPercentage()
        );
    }

    private DiscountDetailResponse toDiscountDetailResponse(DiscountDetail detail) {
        return new DiscountDetailResponse(
                detail.type(),
                detail.name(),
                detail.percentage(),
                detail.amount().amount(),
                detail.sequence()
        );
    }
}
