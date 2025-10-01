package org.banking.app.infrastructure.persistence;

import org.banking.app.domain.accounts.Account;
import org.banking.app.domain.accounts.AccountId;
import org.banking.app.domain.port.AccountRepository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** In-memory repository for demo/testing. */
public final class InMemoryAccountRepository implements AccountRepository {
    private final Map<String, Account> store = new ConcurrentHashMap<>();

    @Override
    public void save(Account account) {
        store.put(account.id().value(), account);
    }

    @Override public Optional<Account> findById(AccountId id) {
        return Optional.ofNullable(store.get(id.value()));
    }
}
