package com.ecommerce.core.checkout.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class MoneyTest {

    @Test
    void shouldRoundAmountsUsingHalfUpWithTwoDecimals() {
        Money money = Money.of(new BigDecimal("10.005"));

        assertThat(money.amount()).isEqualByComparingTo("10.01");
    }

    @Test
    void shouldCalculatePercentageUsingHalfUpWithTwoDecimals() {
        Money discount = Money.of("102.60").percentage(new BigDecimal("15"));

        assertThat(discount.amount()).isEqualByComparingTo("15.39");
    }
}
