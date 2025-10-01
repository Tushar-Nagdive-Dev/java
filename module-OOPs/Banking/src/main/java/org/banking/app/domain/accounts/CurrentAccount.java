package org.banking.app.domain.accounts;

import org.banking.app.enums.AccountType;

import java.math.BigDecimal;

/**
 * Current account with limited overdraft facility.
 */
public final class CurrentAccount extends Account {
    private static final Money OVERDRAFT_LIMIT = Money.of(new BigDecimal("5000.00"));

    public CurrentAccount(AccountId id, Money openingBalance) {
        super(id, AccountType.CURRENT, openingBalance);
    }

    @Override
    protected void validateWithdrawal(Money amount) {
        // Allow balance to go negative but not below -OVERDRAFT_LIMIT
        Money projected = balance().subtract(amount);
        BigDecimal floor = OVERDRAFT_LIMIT.asBigDecimal().negate();
        if (projected.asBigDecimal().compareTo(floor) < 0) {
            throw new RuntimeException("Overdraft limit exceeded");
        }
    }
}
