package org.banking.app.domain.policy;

import org.banking.app.domain.accounts.Account;
import org.banking.app.domain.accounts.CurrentAccount;
import org.banking.app.domain.accounts.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Current account interest is typically zero; demo applies tiny interest if positive. */
public final class CurrentAccountInterestCalculator implements InterestCalculator {
    private static final BigDecimal APR = new BigDecimal("0.005"); // 0.5%

    @Override
    public Money accrue(Account account, int months) {
        if (!(account instanceof CurrentAccount)) return Money.zero();
        if (account.balance().isNegative()) return Money.zero();
        BigDecimal monthly = APR.divide(new BigDecimal("12"), 10, RoundingMode.HALF_UP);
        return account.balance().pct(monthly.multiply(new BigDecimal(months)));
    }

    @Override
    public String name() {
        return "CURRENT_LOW_APR";
    }
}
