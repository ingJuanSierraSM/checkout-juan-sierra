package com.ecommerce.core.checkout.domain.discount;

import com.ecommerce.core.checkout.domain.model.DiscountContext;
import com.ecommerce.core.checkout.domain.model.DiscountDetail;
import com.ecommerce.core.checkout.domain.model.DiscountType;
import com.ecommerce.core.checkout.domain.model.Money;

import java.util.Optional;

public class CouponDiscountStrategy implements DiscountRule {

    private static final int SEQUENCE = 30;

    @Override
    public int sequence() {
        return SEQUENCE;
    }

    @Override
    public Optional<DiscountDetail> apply(DiscountContext context, Money currentSubtotal) {
        return context.coupon()
                .filter(coupon -> coupon.isUsableAt(context.now()))
                .map(coupon -> new DiscountDetail(
                        DiscountType.COUPON,
                        "Cupón " + coupon.code(),
                        coupon.percentage(),
                        currentSubtotal.percentage(coupon.percentage()),
                        SEQUENCE
                ));
    }
}
