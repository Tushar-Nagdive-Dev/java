package org.banking.app.application;

import org.banking.app.application.command.OpenAccountCommand;
import org.banking.app.application.command.TransferCommand;
import org.banking.app.application.dto.AccountView;
import org.banking.app.domain.accounts.Account;
import org.banking.app.domain.accounts.AccountId;
import org.banking.app.domain.accounts.Money;
import org.banking.app.domain.port.AccountRepository;
import org.banking.app.domain.port.NotificationPort;
import org.banking.app.enums.AccountType;
import org.banking.app.infrastructure.persistence.InMemoryAccountRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AccountApplicationServiceTest {

    static class TestNotifier implements NotificationPort {
        final List<String> messages = new ArrayList<>();

        @Override
        public void send(String to, String message) {
            messages.add(to + ":" + message);
        }
    }

    @Test
    void open_deposit_withdraw_transfer_flowWorks() {
        AccountRepository repo = new InMemoryAccountRepository();
        TestNotifier notifier = new TestNotifier();
        AccountApplicationService app = new AccountApplicationService(repo, notifier);

        AccountView a1 = app.open(new OpenAccountCommand("c1@example.com", AccountType.SAVINGS,
                Money.of(new BigDecimal("10000.00"))));
        AccountView a2 = app.open(new OpenAccountCommand("c2@example.com", AccountType.CURRENT,
                Money.of(new BigDecimal("5000.00"))));

        assertThat(notifier.messages).hasSize(2);

        app.deposit(a1.getAccountId(), Money.of(new BigDecimal("1000.00")), "Top-up");
        app.withdraw(a2.getAccountId(), Money.of(new BigDecimal("2000.00")), "Payment");

        app.transfer(new TransferCommand(a1.getAccountId(), a2.getAccountId(), Money.of(new BigDecimal("750.00"))));

        Account acc1 = repo.findById(AccountId.of(a1.getAccountId())).orElseThrow();
        Account acc2 = repo.findById(AccountId.of(a2.getAccountId())).orElseThrow();

        assertThat(acc1.balance().asBigDecimal()).isEqualTo(new BigDecimal("10250.00").setScale(2)); // 10000 +1000 -750
        assertThat(acc2.balance().asBigDecimal()).isEqualTo(new BigDecimal("3750.00").setScale(2));  // 5000 -2000 +750
    }
}
