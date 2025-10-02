package org.banking.app.account;

import org.banking.app.domain.accounts.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class SavingsAccountRulesTest {

    @Test
    void savingsAccount_enforcesMinBalance() {
        Account acc = new SavingsAccount(AccountId.newId(), Money.of(new BigDecimal("5000.00")));

        acc.withdraw(Money.of(new BigDecimal("2000.00")), "Expense");
        assertThat(acc.balance().asBigDecimal()).isEqualTo(new BigDecimal("3000.00").setScale(2));

        // Next withdrawal would break min balance rule (2000.00 min)
        assertThatThrownBy(() -> acc.withdraw(Money.of(new BigDecimal("1500.00")), "Another expense"))
                .isInstanceOf(InsufficientBalanceException.class);
    }
}
