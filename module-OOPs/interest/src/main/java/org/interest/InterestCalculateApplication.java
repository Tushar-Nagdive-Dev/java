package org.interest;

import java.math.BigDecimal;
import java.util.List;

/** Quick demo to show runtime polymorphism in action. */
public class InterestCalculateApplication {

    public static void main(String[] args) {
        InterestCalculator savings = new SavingsInterestCalculator();
        InterestCalculator fd = new FixedDepositInterestCalculator();

        InterestEngine engine = new InterestEngine(List.of(savings, fd));
        String output = engine.report(Money.of(new BigDecimal("100000")), 12); // ₹1,00,000 for 12 months
        System.out.println(output);
    }
}
