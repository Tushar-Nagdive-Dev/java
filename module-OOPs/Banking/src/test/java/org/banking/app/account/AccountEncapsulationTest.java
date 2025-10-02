package org.banking.app.account;

import org.banking.app.domain.accounts.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class AccountEncapsulationTest {

    @Test
    void ledger_isUnmodifiableAndRecordsDomainEventsOnlyThroughMethods() {
        Account acc = new SavingsAccount(AccountId.newId(), Money.of(new BigDecimal("5000.00")));

        acc.deposit(Money.of(new BigDecimal("1000.00")), "Salary");
        acc.withdraw(Money.of(new BigDecimal("500.00")), "ATM");

        assertThat(acc.ledger()).hasSize(4); // opening credit + 2 actions

        // Attempt to mutate externally should fail
        assertThatThrownBy(() -> acc.ledger().add(Transaction.credit(Money.of(new BigDecimal("1.00")), "hack")))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
