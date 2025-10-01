package org.banking.app.application;

import org.banking.app.application.command.OpenAccountCommand;
import org.banking.app.application.command.TransferCommand;
import org.banking.app.application.dto.AccountView;
import org.banking.app.domain.accounts.*;
import org.banking.app.domain.port.AccountRepository;
import org.banking.app.domain.port.NotificationPort;

import java.util.Objects;

import static org.banking.app.enums.AccountType.CURRENT;
import static org.banking.app.enums.AccountType.SAVINGS;

/**
 * Orchestrates use-cases; coordinates domain + ports.
 * <p>Best practice: no business rules here; delegate to domain model.</p>
 */
public final class AccountApplicationService {
    private final AccountRepository repo;
    private final NotificationPort notifier;

    public AccountApplicationService(AccountRepository repo, NotificationPort notifier) {
        this.repo = Objects.requireNonNull(repo);
        this.notifier = Objects.requireNonNull(notifier);
    }

    /** Opens a new account and notifies the customer. */
    public AccountView open(OpenAccountCommand cmd) {
        AccountId id = AccountId.newId();
        Account account = switch (cmd.type()) {
            case SAVINGS -> new SavingsAccount(id, cmd.openingBalance());
            case CURRENT -> new CurrentAccount(id, cmd.openingBalance());
        };
        repo.save(account);
        notifier.send(cmd.customerContact(), "Account opened: " + id.value());
        return AccountView.from(account);
    }

    /** Deposits funds (domain enforces invariants). */
    public AccountView deposit(String id, Money amount, String note) {
        Account account = repo.findById(AccountId.of(id))
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        account.deposit(amount, note);
        repo.save(account);
        return AccountView.from(account);
    }

    /** Withdraws funds (domain validates via subtype policy). */
    public AccountView withdraw(String id, Money amount, String note) {
        Account account = repo.findById(AccountId.of(id))
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + id));
        account.withdraw(amount, note);
        repo.save(account);
        return AccountView.from(account);
    }

    /**
     * Transfers funds between two accounts:
     * 1) withdraw from source (policy enforced by source subtype)
     * 2) deposit into target
     * <p>Best practice: write operations in a consistent order and persist each mutated aggregate.</p>
     */
    public void transfer(TransferCommand cmd) {
        if (cmd.fromAccountId().equals(cmd.toAccountId()))
            throw new IllegalArgumentException("from and to must differ");

        Account from = repo.findById(AccountId.of(cmd.fromAccountId()))
                .orElseThrow(() -> new IllegalArgumentException("From account not found: " + cmd.fromAccountId()));
        Account to = repo.findById(AccountId.of(cmd.toAccountId()))
                .orElseThrow(() -> new IllegalArgumentException("To account not found: " + cmd.toAccountId()));

        // domain rules enforced by each aggregate
        from.withdraw(cmd.amount(), "Transfer to " + to.id().value());
        to.deposit(cmd.amount(), "Transfer from " + from.id().value());

        // persist both
        repo.save(from);
        repo.save(to);
    }
}
