package org.banking.app.domain.accounts;

import org.banking.app.enums.AccountType;

import java.math.BigDecimal;

/**
 * Savings account with a minimum balance rule.
 */
public final class SavingsAccount extends Account {
    private static final Money MIN_BALANCE = Money.of(new BigDecimal("2000.00"));

    public SavingsAccount(AccountId id, Money openingBalance) {
        super(id, AccountType.SAVINGS, openingBalance);
    }

    @Override
    protected void validateWithdrawal(Money amount) {
        // After withdrawal, balance must remain >= MIN_BALANCE
        Money projected = balance().subtract(amount);
        if (projected.asBigDecimal().compareTo(MIN_BALANCE.asBigDecimal()) < 0) {
            throw new InsufficientBalanceException("Savings min balance would be violated");
        }
    }
}
