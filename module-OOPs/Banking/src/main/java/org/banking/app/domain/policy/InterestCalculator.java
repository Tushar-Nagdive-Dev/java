package org.banking.app.domain.policy;

import org.banking.app.domain.accounts.Account;
import org.banking.app.domain.accounts.Money;

/**
 * Calculates interest for an account based on policy.
 */
public interface InterestCalculator {
    Money accrue(Account account, int months);
    String name();
}
