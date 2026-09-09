package com.ecommerce.core.checkout.domain.discount;

import com.ecommerce.core.catalog.domain.model.Category;
import com.ecommerce.core.checkout.domain.model.DiscountContext;
import com.ecommerce.core.checkout.domain.model.DiscountDetail;
import com.ecommerce.core.checkout.domain.model.DiscountType;
import com.ecommerce.core.checkout.domain.model.Money;
import com.ecommerce.core.checkout.domain.model.PricingItem;
import com.ecommerce.core.promotion.domain.model.Coupon;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class DiscountEngineTest {

    private static final Instant NOW = Instant.parse("2026-09-08T12:00:00Z");

    @Test
    void shouldApplyCategoryVolumeAndCouponDiscountsSequentially() {
        var breakdown = engine().calculate(contextWithLaptopAndCoupon(), new MaximumDiscountPolicy(new BigDecimal("35")));

        assertThat(breakdown.discounts()).extracting(discount -> discount.amount().amount())
                .containsExactly(new BigDecimal("12.00"), new BigDecimal("5.40"), new BigDecimal("15.39"));
        assertThat(breakdown.calculatedDiscountBeforeCap().amount()).isEqualByComparingTo("32.79");
        assertThat(breakdown.totalDiscount().amount()).isEqualByComparingTo("32.79");
        assertThat(breakdown.effectiveDiscountPercentage()).isEqualByComparingTo("27.33");
        assertThat(breakdown.finalTotal().amount()).isEqualByComparingTo("87.21");
        assertThat(breakdown.discountCapApplied()).isFalse();
    }

    @Test
    void shouldApplyMaximumDiscountPolicyWhenCalculatedDiscountExceedsCap() {
        var breakdown = engine().calculate(contextWithLaptopAndCoupon(), new MaximumDiscountPolicy(new BigDecimal("25")));

        assertThat(breakdown.calculatedDiscountBeforeCap().amount()).isEqualByComparingTo("32.79");
        assertThat(breakdown.totalDiscount().amount()).isEqualByComparingTo("30.00");
        assertThat(breakdown.effectiveDiscountPercentage()).isEqualByComparingTo("25.00");
        assertThat(breakdown.finalTotal().amount()).isEqualByComparingTo("90.00");
        assertThat(breakdown.discountCapApplied()).isTrue();
    }

    @Test
    void shouldCapDiscountAtExactlyThirtyFivePercentWhenCalculatedDiscountExceedsIt() {
        MaximumDiscountPolicy policy = new MaximumDiscountPolicy(new BigDecimal("35"));

        var breakdown = policy.apply(Money.of("100.00"), List.of(new DiscountDetail(
                DiscountType.CATEGORY,
                "Descuento de prueba",
                new BigDecimal("40.00"),
                Money.of("40.00"),
                10
        )));

        assertThat(breakdown.calculatedDiscountBeforeCap().amount()).isEqualByComparingTo("40.00");
        assertThat(breakdown.totalDiscount().amount()).isEqualByComparingTo("35.00");
        assertThat(breakdown.effectiveDiscountPercentage()).isEqualByComparingTo("35.00");
        assertThat(breakdown.finalTotal().amount()).isEqualByComparingTo("65.00");
        assertThat(breakdown.discountCapApplied()).isTrue();
    }

    @Test
    void shouldNotApplyVolumeDiscountWhenSubtotalIsExactlyOneHundred() {
        DiscountContext context = new DiscountContext(
                List.of(new PricingItem(1L, "Office Chair", Money.of("100.00"), Category.HOME, 1)),
                Optional.empty(),
                NOW
        );

        var breakdown = engine().calculate(context, new MaximumDiscountPolicy(new BigDecimal("35")));

        assertThat(breakdown.discounts()).isEmpty();
        assertThat(breakdown.finalTotal().amount()).isEqualByComparingTo("100.00");
    }

    @Test
    void shouldApplyVolumeDiscountWhenSubtotalExceedsOneHundred() {
        DiscountContext context = new DiscountContext(
                List.of(new PricingItem(1L, "Office Chair", Money.of("100.01"), Category.HOME, 1)),
                Optional.empty(),
                NOW
        );

        var breakdown = engine().calculate(context, new MaximumDiscountPolicy(new BigDecimal("35")));

        assertThat(breakdown.discounts()).singleElement().satisfies(discount -> {
            assertThat(discount.percentage()).isEqualByComparingTo("5.00");
            assertThat(discount.amount().amount()).isEqualByComparingTo("5.00");
        });
        assertThat(breakdown.finalTotal().amount()).isEqualByComparingTo("95.01");
    }

    private DiscountEngine engine() {
        return new DiscountEngine(List.of(
                new CouponDiscountStrategy(),
                new VolumeDiscountStrategy(),
                new CategoryDiscountStrategy()
        ));
    }

    private DiscountContext contextWithLaptopAndCoupon() {
        Coupon coupon = new Coupon(
                1L,
                "WELCOME2026",
                new BigDecimal("15.00"),
                true,
                NOW.plusSeconds(3600),
                null
        );
        return new DiscountContext(
                List.of(new PricingItem(1L, "Laptop Pro", Money.of("120.00"), Category.TECHNOLOGY, 1)),
                Optional.of(coupon),
                NOW
        );
    }
}
