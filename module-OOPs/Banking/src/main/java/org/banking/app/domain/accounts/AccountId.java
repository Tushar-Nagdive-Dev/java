package org.banking.app.domain.accounts;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object representing a unique account identifier.
 * <p>
 * Best practice: wrap IDs in value objects instead of using raw Strings,
 * to improve type safety and avoid accidental misuse.
 * </p>
 */
public final class AccountId {

    private final String value;

    private AccountId(String values) {
        this.value = Objects.requireNonNull(values, "id cannot be null");
    }

    /** Generates a new unique AccountId. */
    public static AccountId newId() {
        return new AccountId(UUID.randomUUID().toString());
    }

    /** Factory method for existing IDs (e.g., loaded from DB). */
    public static AccountId of(String values) {
        return new AccountId(values);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AccountId)) return false;
        AccountId that = (AccountId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
