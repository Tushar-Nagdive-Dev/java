package org.interest;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Fixed Deposit: higher APR with monthly compounding.
 * Best practice: document the formula so behavior is predictable and testable.
 */
public final class FixedDepositInterestCalculator implements InterestCalculator {
    private static final BigDecimal APR = new BigDecimal("0.0725"); // 7.25% per year

    @Override
    public Money calculate(Money principal, int months) {
        if (months < 1) throw new IllegalArgumentException("months must be >= 1");
        BigDecimal monthlyRate = APR.divide(new BigDecimal("12"), 10, RoundingMode.HALF_UP);
        // compound: P * ((1 + r)^n - 1)
        BigDecimal factor = BigDecimal.ONE.add(monthlyRate).pow(months);
        BigDecimal interest = principal.asBigDecimal().multiply(factor.subtract(BigDecimal.ONE));
        return Money.of(interest);
    }

    @Override public String name() { return "FD_COMPOUND_APR"; }
}
