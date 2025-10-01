package org.banking.app.domain.accounts;

/**
 * Domain-specific runtime exception for insufficient balance or overdraft violations.
 *
 * <p>Best practice:
 * <ul>
 *   <li>Use custom exceptions for domain errors instead of generic IllegalStateException.</li>
 *   <li>Extend RuntimeException for business rule violations (checked exceptions make code noisy).</li>
 *   <li>Keep the exception name expressive and focused on the domain concept.</li>
 * </ul>
 * </p>
 */
public class InsufficientBalanceException extends RuntimeException {

    /**
     * Creates a new exception with a descriptive message.
     * @param message explanation of why the balance check failed
     */
    public InsufficientBalanceException(String message) {
        super(message);
    }
}
