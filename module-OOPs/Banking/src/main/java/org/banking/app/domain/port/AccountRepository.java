package org.banking.app.domain.port;

import org.banking.app.domain.accounts.Account;
import org.banking.app.domain.accounts.AccountId;

import java.util.Optional;

/**
 * Repository port for accounts.
 * <p>Abstraction: hides storage (JPA, Mongo, etc.).</p>
 */
public interface AccountRepository {
    void save(Account account);
    Optional<Account> findById(AccountId id);
}
