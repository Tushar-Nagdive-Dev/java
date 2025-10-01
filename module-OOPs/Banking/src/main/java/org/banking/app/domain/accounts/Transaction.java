package org.banking.app.domain.accounts;

import org.banking.app.enums.TransactionType;

import java.time.Instant;
import java.util.Objects;

/**
 * Immutable ledger entry representing a single account transaction.
 *
 * <p>Design goals (best practices):
 * <ul>
 *   <li><b>Immutability:</b> all fields are final; object state cannot change after creation.</li>
 *   <li><b>Type-safety:</b> amount is {@link Money}; type is {@link TransactionType}.</li>
 *   <li><b>Integrity:</b> factory methods validate inputs (no nulls, no negative amounts).</li>
 *   <li><b>Traceability:</b> each entry captures a timestamp and a human-readable note.</li>
 * </ul>
 * </p>
 */
public final class Transaction {
    /** CREDIT or DEBIT. */
    private final TransactionType type;

    /** Always non-negative monetary magnitude of this entry. */
    private final Money amount;

    /** UTC timestamp when this transaction occurred (recorded). */
    private final Instant occurredAt;

    /** Optional human-readable context (e.g., "Opening balance", "ATM withdrawal"). */
    private final String note;

    private Transaction(TransactionType type, Money amount, Instant occurredAt, String note) {
        this.type = Objects.requireNonNull(type, "type cannot be null");
        this.amount = validateAmount(amount);
        this.occurredAt = Objects.requireNonNull(occurredAt, "occurredAt cannot be null");
        this.note = sanitizeNote(note);
    }

    /**
     * Creates a CREDIT transaction entry.
     * @param amount    non-negative amount to credit
     * @param note      optional note (null allowed)
     * @return immutable Transaction
     */
    public static Transaction credit(Money amount, String note) {
        return new Transaction(TransactionType.CREDIT, amount, Instant.now(), note);
    }

    /**
     * Creates a DEBIT transaction entry.
     * @param amount    non-negative amount to debit
     * @param note      optional note (null allowed)
     * @return immutable Transaction
     */
    public static Transaction debit(Money amount, String note) {
        return new Transaction(TransactionType.DEBIT, amount, Instant.now(), note);
    }

    /**
     * Internal validator to ensure we never record a negative amount.
     * (The sign is represented by {@link TransactionType}, not the Money value.)
     */
    private static Money validateAmount(Money money) {
        Objects.requireNonNull(money, "amount cannot be null");
        if (money.asBigDecimal().signum() < 0) {
            throw new IllegalArgumentException("Transaction amount must be non-negative");
        }
        return money;
    }

    /**
     * Trims and bounds the note to a reasonable length to prevent log/DB abuse.
     * (Adjust the max length based on your storage limits.)
     */
    private static String sanitizeNote(String note) {
        if (note == null) return null;
        String trimmed = note.trim();
        int MAX = 255;
        return trimmed.length() <= MAX ? trimmed : trimmed.substring(0, MAX);
    }

    // -------- Getters (no setters to keep immutability) --------

    /** @return CREDIT or DEBIT */
    public TransactionType getType() { return type; }

    /** @return non-negative amount associated with this entry */
    public Money getAmount() { return amount; }

    /** @return UTC timestamp when this entry was recorded */
    public Instant getOccurredAt() { return occurredAt; }

    /** @return optional human-readable note (may be null) */
    public String getNote() { return note; }

    // -------- Equality & toString (useful for tests and logs) --------

    @Override
    public String toString() {
        return "Transaction{" +
                "type=" + type +
                ", amount=" + amount +
                ", occurredAt=" + occurredAt +
                (note != null ? ", note='" + note + '\'' : "") +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction)) return false;
        Transaction that = (Transaction) o;
        return type == that.type &&
                Objects.equals(amount, that.amount) &&
                Objects.equals(occurredAt, that.occurredAt) &&
                Objects.equals(note, that.note);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, amount, occurredAt, note);
    }
}
