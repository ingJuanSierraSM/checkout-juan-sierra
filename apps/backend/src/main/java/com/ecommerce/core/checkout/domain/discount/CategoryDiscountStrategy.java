package com.ecommerce.core.checkout.domain.discount;

import com.ecommerce.core.catalog.domain.model.Category;
import com.ecommerce.core.checkout.domain.model.DiscountContext;
import com.ecommerce.core.checkout.domain.model.DiscountDetail;
import com.ecommerce.core.checkout.domain.model.DiscountType;
import com.ecommerce.core.checkout.domain.model.Money;

import java.math.BigDecimal;
import java.util.Optional;

public class CategoryDiscountStrategy implements DiscountRule {

    private static final int SEQUENCE = 10;
    private static final BigDecimal PERCENTAGE = new BigDecimal("10.00");

    @Override
    public int sequence() {
        return SEQUENCE;
    }

    @Override
    public Optional<DiscountDetail> apply(DiscountContext context, Money currentSubtotal) {
        Money technologySubtotal = context.subtotalFor(Category.TECHNOLOGY);
        if (technologySubtotal.isZero()) {
            return Optional.empty();
        }

        Money amount = technologySubtotal.percentage(PERCENTAGE);
        return Optional.of(new DiscountDetail(
                DiscountType.CATEGORY,
                "Descuento Tecnología",
                PERCENTAGE,
                amount,
                SEQUENCE
        ));
    }
}
