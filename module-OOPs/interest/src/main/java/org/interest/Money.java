package org.interest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Represents monetary value in a safe and immutable way.
 * <p>Best practice: avoid double for money; prefer BigDecimal with explicit scale and rounding.</p>
 */
public final class Money {

    private final BigDecimal amount;

    private Money(BigDecimal amount) {
        if(amount == null) throw new IllegalArgumentException("Amount cannot be null");
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    // 💡 FIX 1: Add the required method to expose the internal BigDecimal amount.
    public BigDecimal asBigDecimal() {
        return this.amount;
    }

    /** Factory method to create Money from BigDecimal. */
    public static Money of(BigDecimal amount) {
        return new Money(amount);
    }

    /** Adds two Money amounts, returning a new instance (immutability). */
    public Money add(Money other) {
        return of(this.amount.add(other.amount));
    }

    /** Multiplies by a decimal percentage, e.g., interestRate=0.045 for 4.5%. */
    public Money pct(BigDecimal rate) {
        return of(this.amount.multiply(rate));
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money money)) return false;
        return amount.compareTo(money.amount) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount);
    }

    @Override
    public String toString() {
        return "₹" + amount.toPlainString();
    }
}