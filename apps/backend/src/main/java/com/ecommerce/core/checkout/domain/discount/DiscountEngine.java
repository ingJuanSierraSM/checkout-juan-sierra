package com.ecommerce.core.checkout.domain.discount;

import com.ecommerce.core.checkout.domain.model.DiscountBreakdown;
import com.ecommerce.core.checkout.domain.model.DiscountContext;
import com.ecommerce.core.checkout.domain.model.DiscountDetail;
import com.ecommerce.core.checkout.domain.model.Money;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class DiscountEngine {

    private final List<DiscountRule> rules;

    public DiscountEngine(List<DiscountRule> rules) {
        Objects.requireNonNull(rules, "Discount rules are required");
        this.rules = rules.stream()
                .sorted(Comparator.comparingInt(DiscountRule::sequence))
                .toList();
    }

    public DiscountBreakdown calculate(DiscountContext context, MaximumDiscountPolicy maximumDiscountPolicy) {
        Objects.requireNonNull(context, "Discount context is required");
        Objects.requireNonNull(maximumDiscountPolicy, "Maximum discount policy is required");

        Money originalSubtotal = context.originalSubtotal();
        Money currentSubtotal = originalSubtotal;
        List<DiscountDetail> discounts = new ArrayList<>();

        for (DiscountRule rule : rules) {
            var discount = rule.apply(context, currentSubtotal);
            if (discount.isPresent()) {
                discounts.add(discount.get());
                currentSubtotal = currentSubtotal.subtract(discount.get().amount());
            }
        }

        return maximumDiscountPolicy.apply(originalSubtotal, discounts);
    }
}
