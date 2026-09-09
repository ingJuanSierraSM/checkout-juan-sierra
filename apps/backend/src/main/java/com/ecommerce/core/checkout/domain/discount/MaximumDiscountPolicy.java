package com.ecommerce.core.checkout.domain.discount;

import com.ecommerce.core.checkout.domain.model.DiscountBreakdown;
import com.ecommerce.core.checkout.domain.model.DiscountDetail;
import com.ecommerce.core.checkout.domain.model.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

public class MaximumDiscountPolicy {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final BigDecimal maximumDiscountPercentage;

    public MaximumDiscountPolicy(BigDecimal maximumDiscountPercentage) {
        Objects.requireNonNull(maximumDiscountPercentage, "Maximum discount percentage is required");
        if (maximumDiscountPercentage.signum() < 0
                || maximumDiscountPercentage.compareTo(ONE_HUNDRED) > 0) {
            throw new IllegalArgumentException("Maximum discount percentage must be between 0 and 100");
        }
        this.maximumDiscountPercentage = maximumDiscountPercentage.setScale(2, RoundingMode.HALF_UP);
    }

    public DiscountBreakdown apply(Money originalSubtotal, List<DiscountDetail> discounts) {
        Money calculatedDiscount = discounts.stream()
                .map(DiscountDetail::amount)
                .reduce(Money.zero(), Money::add);
        Money maximumDiscount = originalSubtotal.percentage(maximumDiscountPercentage);
        boolean capApplied = calculatedDiscount.compareTo(maximumDiscount) > 0;
        Money totalDiscount = capApplied ? maximumDiscount : calculatedDiscount;
        Money finalTotal = originalSubtotal.subtract(totalDiscount);
        BigDecimal effectivePercentage = originalSubtotal.isZero()
                ? BigDecimal.ZERO
                : totalDiscount.amount()
                .multiply(ONE_HUNDRED)
                .divide(originalSubtotal.amount(), 2, RoundingMode.HALF_UP);

        return new DiscountBreakdown(
                originalSubtotal,
                discounts,
                calculatedDiscount,
                totalDiscount,
                effectivePercentage,
                finalTotal,
                capApplied,
                maximumDiscountPercentage
        );
    }
}
