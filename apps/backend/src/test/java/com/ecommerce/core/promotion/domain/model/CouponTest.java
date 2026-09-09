package com.ecommerce.core.promotion.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class CouponTest {

    private static final Instant NOW = Instant.parse("2026-09-08T12:00:00Z");

    @Test
    void shouldBeUsableWhenActiveNotExpiredAndUnused() {
        Coupon coupon = coupon(true, NOW.plusSeconds(3600), null);

        assertThat(coupon.isUsableAt(NOW)).isTrue();
    }

    @Test
    void shouldNotBeUsableWhenInactive() {
        Coupon coupon = coupon(false, NOW.plusSeconds(3600), null);

        assertThat(coupon.isUsableAt(NOW)).isFalse();
    }

    @Test
    void shouldNotBeUsableWhenExpired() {
        Coupon coupon = coupon(true, NOW.minusSeconds(1), null);

        assertThat(coupon.isUsableAt(NOW)).isFalse();
    }

    @Test
    void shouldNotBeUsableWhenAlreadyUsed() {
        Coupon coupon = coupon(true, NOW.plusSeconds(3600), NOW.minusSeconds(30));

        assertThat(coupon.isUsableAt(NOW)).isFalse();
    }

    private Coupon coupon(boolean active, Instant expiresAt, Instant usedAt) {
        return new Coupon(1L, "WELCOME2026", new BigDecimal("15.00"), active, expiresAt, usedAt);
    }
}
