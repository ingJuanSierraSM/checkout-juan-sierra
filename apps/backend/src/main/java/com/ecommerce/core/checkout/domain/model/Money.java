package com.ecommerce.core.checkout.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record Money(BigDecimal amount) implements Comparable<Money> {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    public Money {
        Objects.requireNonNull(amount, "Money amount is required");
        amount = amount.setScale(SCALE, ROUNDING_MODE);
    }

    public static Money of(BigDecimal amount) {
        return new Money(amount);
    }

    public static Money of(String amount) {
        return new Money(new BigDecimal(amount));
    }

    public static Money zero() {
        return new Money(BigDecimal.ZERO);
    }

    public Money add(Money other) {
        return new Money(amount.add(other.amount));
    }

    public Money subtract(Money other) {
        Money result = new Money(amount.subtract(other.amount));
        if (result.amount.signum() < 0) {
            throw new IllegalArgumentException("Money amount cannot be negative");
        }
        return result;
    }

    public Money multiply(int multiplier) {
        if (multiplier < 0) {
            throw new IllegalArgumentException("Multiplier cannot be negative");
        }
        return new Money(amount.multiply(BigDecimal.valueOf(multiplier)));
    }

    public Money percentage(BigDecimal percentage) {
        Objects.requireNonNull(percentage, "Percentage is required");
        if (percentage.signum() < 0 || percentage.compareTo(ONE_HUNDRED) > 0) {
            throw new IllegalArgumentException("Percentage must be between 0 and 100");
        }
        return new Money(amount.multiply(percentage).divide(ONE_HUNDRED, SCALE, ROUNDING_MODE));
    }

    public boolean isZero() {
        return amount.signum() == 0;
    }

    @Override
    public int compareTo(Money other) {
        return amount.compareTo(other.amount);
    }
}
