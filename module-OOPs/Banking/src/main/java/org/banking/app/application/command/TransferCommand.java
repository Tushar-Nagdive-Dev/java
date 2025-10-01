package org.banking.app.application.command;

import org.banking.app.domain.accounts.Money;

import java.util.Objects;

/**
 * Command object for transferring money between accounts.
 *
 * <p>Best practice: Keep commands simple carriers of user input.
 * Validation logic belongs here for basic non-null checks, while
 * domain invariants (like overdraft rules) live in the domain layer.</p>
 */
public final class TransferCommand {
    private final String fromAccountId;
    private final String toAccountId;
    private final Money amount;

    public TransferCommand(String fromAccountId, String toAccountId, Money amount) {
        this.fromAccountId = Objects.requireNonNull(fromAccountId, "fromAccountId required");
        this.toAccountId = Objects.requireNonNull(toAccountId, "toAccountId required");
        this.amount = Objects.requireNonNull(amount, "amount required");
    }

    public String fromAccountId() { return fromAccountId; }
    public String toAccountId() { return toAccountId; }
    public Money amount() { return amount; }
}
