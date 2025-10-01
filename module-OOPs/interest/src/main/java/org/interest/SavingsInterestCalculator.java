package org.interest;

import java.math.BigDecimal;

/**
 * Simple savings policy: APR with monthly proration. No compounding in this demo.
 */
public final class SavingsInterestCalculator implements InterestCalculator {
    private static final BigDecimal APR = new BigDecimal("0.035"); // 3.5% per year

    @Override
    public Money calculate(Money principal, int months) {
        if (months < 1) throw new IllegalArgumentException("months must be >= 1");
        BigDecimal monthlyRate = APR.divide(new BigDecimal("12"), 10, java.math.RoundingMode.HALF_UP);
        // interest = principal * monthlyRate * months
        return principal.pct(monthlyRate.multiply(new BigDecimal(months)));
    }

    @Override public String name() { return "SAVINGS_SIMPLE_APR"; }
}
