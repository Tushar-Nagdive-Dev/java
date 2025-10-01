package org.banking.app;

import org.banking.app.application.AccountApplicationService;
import org.banking.app.application.command.OpenAccountCommand;
import org.banking.app.application.command.TransferCommand;
import org.banking.app.application.dto.AccountView;
import org.banking.app.domain.accounts.Account;
import org.banking.app.domain.accounts.AccountId;
import org.banking.app.domain.accounts.Money;
import org.banking.app.domain.policy.CurrentAccountInterestCalculator;
import org.banking.app.domain.policy.InterestCalculator;
import org.banking.app.domain.policy.SavingsInterestCalculator;
import org.banking.app.domain.port.AccountRepository;
import org.banking.app.enums.AccountType;
import org.banking.app.infrastructure.notification.ConsoleNotificationAdapter;
import org.banking.app.infrastructure.persistence.InMemoryAccountRepository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Minimal bootstrap to demonstrate opening accounts, depositing,
 * withdrawing, transferring, and interest strategy reporting.
 *
 * <p>Run this main class from your IDE or via 'java -cp ... com.tushar.arch.bank.Bootstrap'</p>
 */
public final class BankingApplication {

    public static void main(String[] args) {
        // --- Wire up simple infrastructure ---
        AccountRepository repo = new InMemoryAccountRepository();
        ConsoleNotificationAdapter notifier = new ConsoleNotificationAdapter();
        AccountApplicationService app = new AccountApplicationService(repo, notifier);

        // --- Open two accounts ---
        AccountView a1 = app.open(new OpenAccountCommand(
                "customer1@example.com", AccountType.SAVINGS, Money.of(new BigDecimal("10000.00"))));
        AccountView a2 = app.open(new OpenAccountCommand(
                "customer2@example.com", AccountType.CURRENT, Money.of(new BigDecimal("5000.00"))));

        System.out.println("Opened: " + a1);
        System.out.println("Opened: " + a2);

        // --- Deposit and Withdraw ---
        AccountView a1AfterDeposit = app.deposit(a1.getAccountId(), Money.of(new BigDecimal("1500.00")), "Salary top-up");
        System.out.println("After deposit (A1): " + a1AfterDeposit);

        AccountView a2AfterWithdraw = app.withdraw(a2.getAccountId(), Money.of(new BigDecimal("2000.00")), "Vendor payment");
        System.out.println("After withdraw (A2): " + a2AfterWithdraw);

        // --- Transfer ---
        app.transfer(new TransferCommand(a1.getAccountId(), a2.getAccountId(), Money.of(new BigDecimal("750.00"))));
        System.out.println("Transfer 750.00 from A1 -> A2 complete");

        // Fetch to view updated balances (simple fetch using repository)
        Account acc1 = repo.findById(AccountId.of(a1.getAccountId())).orElseThrow();
        Account acc2 = repo.findById(AccountId.of(a2.getAccountId())).orElseThrow();
        System.out.println("Post-transfer A1 balance: " + acc1.balance());
        System.out.println("Post-transfer A2 balance: " + acc2.balance());

        // --- Interest strategies (polymorphism demo in the banking domain) ---
        List<InterestCalculator> calculators = List.of(
                new SavingsInterestCalculator(),
                new CurrentAccountInterestCalculator()
        );

        System.out.println("\n=== Interest Report for 12 months ===");
        calculators.forEach(calc -> {
            Money i1 = calc.accrue(acc1, 12);
            Money i2 = calc.accrue(acc2, 12);
            System.out.printf("%s -> A1: %s | A2: %s%n", calc.name(), i1, i2);
        });

        // --- Print a small ledger sample for A1 ---
        System.out.println("\n=== A1 Ledger (sample) ===");
        acc1.ledger().forEach(System.out::println);
    }

}
