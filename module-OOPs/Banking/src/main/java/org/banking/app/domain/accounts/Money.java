package org.banking.app.domain.accounts;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Immutable monetary value object.
 * <p>Encapsulation: prevents illegal states and centralizes money math.</p>
 */
public class Money {

    private final BigDecimal amount;

    private Money(BigDecimal amount) {
        if(amount == null) throw new IllegalArgumentException("Amount cannot be null");
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public static Money of(BigDecimal amount) {
        return new Money(amount);
    }

    public static Money zero() {
        return new Money(BigDecimal.ZERO);
    }

    /** Multiplies by a decimal percentage, e.g., interestRate=0.045 for 4.5%. */
    public Money pct(BigDecimal rate) {
        return of(this.amount.multiply(rate));
    }

    public Money add(Money other) { return of(this.amount.add(other.amount)); }
    public Money subtract(Money other) { return of(this.amount.subtract(other.amount)); }
    public boolean isNegative() { return amount.signum() < 0; }

    public BigDecimal asBigDecimal() { return amount; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money)) return false;
        return amount.compareTo(((Money) o).amount) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount.stripTrailingZeros());
    }

    @Override
    public String toString() {
        return "₹" + amount.toPlainString();
    }
}
