package org.banking.app.enums;

/**
 * Categorical type for a transaction entry.
 * <p>Best practice: use enums for fixed categories instead of free-form strings.</p>
 */
public enum TransactionType {
    CREDIT,  // money added to the account
    DEBIT    // money removed from the account
}
