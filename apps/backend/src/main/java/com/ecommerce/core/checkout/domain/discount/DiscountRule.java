package com.ecommerce.core.checkout.domain.discount;

import com.ecommerce.core.checkout.domain.model.DiscountContext;
import com.ecommerce.core.checkout.domain.model.DiscountDetail;
import com.ecommerce.core.checkout.domain.model.Money;

import java.util.Optional;

public interface DiscountRule {

    int sequence();

    Optional<DiscountDetail> apply(DiscountContext context, Money currentSubtotal);
}
