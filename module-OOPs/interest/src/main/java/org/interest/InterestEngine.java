package org.interest;

import java.util.List;

/**
 * Orchestrates interest calculation using injected strategies.
 * <p>Best practice: depend on abstractions, not concrete classes.</p>
 */
public final class InterestEngine {
    private final List<InterestCalculator> calculators;

    public InterestEngine(List<InterestCalculator> calculators) {
        if (calculators == null || calculators.isEmpty())
            throw new IllegalArgumentException("At least one InterestCalculator is required");
        this.calculators = List.copyOf(calculators);
    }

    /**
     * Applies all strategies and returns a simple textual report.
     * In real apps, this could return a DTO. Kept simple for the demo.
     */
    public String report(Money principal, int months) {
        StringBuilder sb = new StringBuilder("Interest Report for " + principal + " / " + months + "m\n");
        calculators.forEach(c -> {
            Money interest = c.calculate(principal, months);
            sb.append("- ").append(c.name()).append(": ").append(interest).append("\n");
        });
        return sb.toString();
    }
}
