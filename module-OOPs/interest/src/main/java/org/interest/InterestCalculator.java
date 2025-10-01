package org.interest;
/**
 * Strategy contract for computing interest on a principal amount.
 * Implementations encapsulate different policies without callers knowing the details.
 */

public interface InterestCalculator {
    /**
     * Calculates interest for a given principal and term (months).
     * @param principal the principal amount (Money)
     * @param months term in months (must be >= 1)
     * @return the interest amount (Money), never negative
     */
    Money calculate(Money principal, int months);

    /** @return a human-readable name for observability/logging. */
    String name();
}