package org.banking.app.application.dto;

import org.banking.app.domain.accounts.Account;
import org.banking.app.domain.accounts.Money;
import org.banking.app.enums.AccountType;

/**
 * Data Transfer Object (DTO) for exposing account details to clients.
 *
 * <p>Best practice: DTOs decouple domain entities from external layers
 * (API, UI). They should be immutable and only expose what’s needed.</p>
 */
public final class AccountView {
    private final String accountId;
    private final AccountType type;
    private final Money balance;

    public AccountView(String accountId, AccountType type, Money balance) {
        this.accountId = accountId;
        this.type = type;
        this.balance = balance;
    }

    public String getAccountId() { return accountId; }
    public AccountType getType() { return type; }
    public Money getBalance() { return balance; }

    /** Factory method to convert a domain {@link Account} into an AccountView. */
    public static AccountView from(Account account) {
        return new AccountView(account.id().value(), account.type(), account.balance());
    }

    @Override
    public String toString() {
        return "AccountView{" +
                "accountId='" + accountId + '\'' +
                ", type=" + type +
                ", balance=" + balance +
                '}';
    }
}
