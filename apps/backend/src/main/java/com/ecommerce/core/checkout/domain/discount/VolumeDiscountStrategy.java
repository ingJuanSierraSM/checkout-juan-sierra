package com.ecommerce.core.checkout.domain.discount;

import com.ecommerce.core.checkout.domain.model.DiscountContext;
import com.ecommerce.core.checkout.domain.model.DiscountDetail;
import com.ecommerce.core.checkout.domain.model.DiscountType;
import com.ecommerce.core.checkout.domain.model.Money;

import java.math.BigDecimal;
import java.util.Optional;

public class VolumeDiscountStrategy implements DiscountRule {

    private static final int SEQUENCE = 20;
    private static final BigDecimal PERCENTAGE = new BigDecimal("5.00");
    private static final Money THRESHOLD = Money.of("100.00");

    @Override
    public int sequence() {
        return SEQUENCE;
    }

    @Override
    public Optional<DiscountDetail> apply(DiscountContext context, Money currentSubtotal) {
        if (currentSubtotal.compareTo(THRESHOLD) <= 0) {
            return Optional.empty();
        }

        return Optional.of(new DiscountDetail(
                DiscountType.VOLUME,
                "Descuento por volumen",
                PERCENTAGE,
                currentSubtotal.percentage(PERCENTAGE),
                SEQUENCE
        ));
    }
}
