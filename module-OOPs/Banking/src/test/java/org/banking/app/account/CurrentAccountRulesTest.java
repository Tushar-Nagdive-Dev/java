package org.banking.app.account;

import org.banking.app.domain.accounts.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class CurrentAccountRulesTest {

    @Test
    void currentAccount_allowsOverdraftWithinLimit() {
        Account acc = new CurrentAccount(AccountId.newId(), Money.of(new BigDecimal("1000.00")));

        acc.withdraw(Money.of(new BigDecimal("5500.00")), "Large payment"); // balance becomes -4500
        assertThat(acc.balance().asBigDecimal()).isEqualTo(new BigDecimal("-4500.00").setScale(2));

        // Exceed overdraft (limit -5000)
        assertThatThrownBy(() -> acc.withdraw(Money.of(new BigDecimal("1000.00")), "Too much"))
                .isInstanceOf(InsufficientBalanceException.class);
    }
}
