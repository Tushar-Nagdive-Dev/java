package org.banking.app.application.command;

import org.banking.app.domain.accounts.Money;
import org.banking.app.enums.AccountType;

import java.util.Objects;

/**
 * Command object for opening a new account.
 *
 * <p>Best practice: Commands are immutable, represent user intent,
 * and validate their required fields eagerly.</p>
 */
public final class OpenAccountCommand {
    private final String customerContact; // e.g., email or phone number
    private final AccountType type;
    private final Money openingBalance;

    public OpenAccountCommand(String customerContact, AccountType type, Money openingBalance) {
        this.customerContact = Objects.requireNonNull(customerContact, "contact required");
        this.type = Objects.requireNonNull(type, "account type required");
        this.openingBalance = Objects.requireNonNull(openingBalance, "opening balance required");
    }

    public String customerContact() { return customerContact; }
    public AccountType type() { return type; }
    public Money openingBalance() { return openingBalance; }
}
