package org.banking.app.account;

import org.banking.app.domain.accounts.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * Why: catches rounding/scale mistakes, immutability, and math correctness. In real apps, money bugs are costly.
 * VOs should be immutable and do pure math, not business rules.
 * Business invariants (e.g., “balance cannot be negative”) belong in aggregates like Account.
 */
public class MoneyTest {

    @Test
    public void of_normalizesScaleAndKeepsImmutability() {
        Money money1 = Money.of(new BigDecimal("10"));
        Money money2 = Money.of(new BigDecimal("10.0"));
        Money money3 = Money.of(new BigDecimal("10.00"));

        // All equal after scale normalization
        assertThat(money1).isEqualTo(money2).isEqualTo(money3);

        // Immutability: add returns new object, doesn’t mutate original
        Money sum = money1.add(Money.of(new BigDecimal("5.50")));
        assertThat(sum.asBigDecimal()).isEqualTo(new BigDecimal("15.50").setScale(2));
        assertThat(money1.asBigDecimal()).isEqualTo(new BigDecimal("10.00").setScale(2));
    }

    @Test
    void pct_calculatesPercentageAccurately() {
        Money principal = Money.of(new BigDecimal("100000"));
        Money fivePercent = principal.pct(new BigDecimal("0.05"));

        assertThat(fivePercent.asBigDecimal()).isEqualTo(new BigDecimal("5000.00").setScale(2));
    }

    @Test
    void subtract_canGoNegative_butCallersMustGuardBusinessRules() {
        Money a = Money.of(new BigDecimal("100.00"));
        Money result = a.subtract(Money.of(new BigDecimal("150.00")));
        assertThat(result.isNegative()).isTrue(); // Domain (Account) enforces rule, VO stays general
    }
}
