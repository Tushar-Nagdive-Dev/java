package org.banking.app.policy;

import org.banking.app.domain.accounts.*;
import org.banking.app.domain.policy.CurrentAccountInterestCalculator;
import org.banking.app.domain.policy.InterestCalculator;
import org.banking.app.domain.policy.SavingsInterestCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class InterestCalculatorContractTest {

    private Account savings;
    private Account current;
    private Money principal;

    private List<InterestCalculator> implementations;

    @BeforeEach
    void setUp() {
        savings = new SavingsAccount(AccountId.newId(), Money.of(new BigDecimal("10000.00")));
        current = new CurrentAccount(AccountId.newId(), Money.of(new BigDecimal("5000.00")));
        principal = Money.of(new BigDecimal("10000.00"));

        implementations = List.of(
                new SavingsInterestCalculator(),
                new CurrentAccountInterestCalculator()
        );
    }

    @Test
    void interest_isNonNegative_andDeterministicForSameInput() {
        implementations.forEach(calc -> {
            Money i1 = calc.accrue(savings, 12);
            Money i2 = calc.accrue(savings, 12);

            assertThat(i1.asBigDecimal().signum()).isGreaterThanOrEqualTo(0);
            assertThat(i1).isEqualTo(i2);
        });
    }

    @Test
    void savingsGetsMeaningfulInterest_currentGetsLittleOrZero() {
        Money sI = new SavingsInterestCalculator().accrue(savings, 12);
        Money cI = new CurrentAccountInterestCalculator().accrue(current, 12);

        // sanity (values depend on your APRs)
        assertThat(sI.asBigDecimal().doubleValue()).isGreaterThan(0.0);
        assertThat(cI.asBigDecimal().doubleValue()).isGreaterThanOrEqualTo(0.0);
    }
}
