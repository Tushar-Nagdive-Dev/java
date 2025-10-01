package org.banking.app.domain.accounts;

import org.banking.app.enums.AccountType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Abstract bank account with guarded state and behaviors.
 * <p>Encapsulation: balance and ledger are private; mutations go through methods.</p>
 * <p>Inheritance: concrete accounts refine withdraw policy.</p>
 */
public abstract class Account {
    private final AccountId id;
    private final AccountType type;
    private Money balance;
    private final List<Transaction> ledger = new ArrayList<>();

    protected Account(AccountId id, AccountType type, Money openingBalance) {
        this.id = id;
        this.type = type;
        this.balance = openingBalance;
        record(Transaction.credit(openingBalance, "Opening balance"));
    }

    /** Template method: concrete types enforce withdrawal policy. */
    protected abstract void validateWithdrawal(Money amount);

    public final void deposit(Money amount, String note) {
        if (amount == null) throw new IllegalArgumentException("amount null");
        this.balance = this.balance.add(amount);
        record(Transaction.credit(amount, note));
    }

    public final void withdraw(Money amount, String note) {
        if (amount == null) throw new IllegalArgumentException("amount null");
        validateWithdrawal(amount);                 // polymorphic rule
        this.balance = this.balance.subtract(amount);
        if (this.balance.isNegative()) throw new RuntimeException("Insufficient funds");
        record(Transaction.debit(amount, note));
    }

    protected void record(Transaction t) { ledger.add(t); }

    public AccountId id() { return id; }
    public AccountType type() { return type; }
    public Money balance() { return balance; }
    public List<Transaction> ledger() { return Collections.unmodifiableList(ledger); }
}
