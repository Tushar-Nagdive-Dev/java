package org.banking.app.domain.policy;

import org.banking.app.domain.accounts.Account;
import org.banking.app.domain.accounts.Money;
import org.banking.app.domain.accounts.SavingsAccount;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Simple savings interest without compounding for demo. */
public final class SavingsInterestCalculator implements InterestCalculator {
    private static final BigDecimal APR = new BigDecimal("0.035");

    @Override
    public Money accrue(Account account, int months) {
        if (!(account instanceof SavingsAccount)) return Money.zero();
        BigDecimal monthly = APR.divide(new BigDecimal("12"), 10, RoundingMode.HALF_UP);
        return account.balance().pct(monthly.multiply(new BigDecimal(months)));
    }

    @Override
    public String name() {
        return "SAVINGS_SIMPLE_APR";
    }

}
