package com.ecommerce.core.promotion.infrastructure.persistence;

import com.ecommerce.core.promotion.application.service.GetCouponService;
import com.ecommerce.core.promotion.application.service.GetDiscountPolicyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PromotionRepositoryIntegrationTest {

    @Autowired
    private GetCouponService getCouponService;

    @Autowired
    private GetDiscountPolicyService getDiscountPolicyService;

    @Test
    void shouldFindSeedCouponAndMaximumDiscountPolicy() {
        var coupon = getCouponService.findByCode("WELCOME2026");
        var policy = getDiscountPolicyService.findByCode("MAX_TOTAL_DISCOUNT_PERCENTAGE");

        assertThat(coupon).isPresent();
        assertThat(coupon.orElseThrow().percentage()).isEqualByComparingTo("15.00");
        assertThat(policy).isPresent();
        assertThat(policy.orElseThrow().value()).isEqualByComparingTo("35.00");
    }
}
