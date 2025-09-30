# Encapsulation — Deep, Practical, Enterprise View

## What it really means

Encapsulation is **controlling access to state + enforcing invariants through behavior**. In enterprise systems, it’s not “getters/setters everywhere.” It’s: *“Clients can’t put my object into an illegal state; all changes go through business rules.”*

**Mental model:** Treat a class like a **secure API** around sensitive data. Only approved operations (methods) can change state, and every operation enforces rules.

---

## Why it matters in real projects

* **Correctness**: Prevents illegal states (e.g., negative balance, shipped order without payment).
* **Evolvability**: You can change internals without breaking callers.
* **Security & Compliance**: Least privilege; limit what external code can do with sensitive fields (PII, card tokens).
* **Concurrency**: Encapsulated state is easier to make thread-safe (immutable VOs, private locks).

---

## What should be encapsulated?

1. **State** (fields) → keep them `private`.
2. **Invariants** (rules that must always hold) → enforce in constructor/factory and each mutator.
3. **Collections** → never expose mutable internals; return read-only views or defensive copies.
4. **Temporal logic** (e.g., status transitions) → only valid transitions allowed through methods.
5. **Integration details** → hide IDs/tokens from leaking (e.g., payment provider tokens).

---

## Enterprise examples (concrete + varied)

1. **Banking**: `Account.debit(amount)` ensures balance never goes negative; only this method can reduce balance.
2. **E-commerce**: `Order.addLineItem()` rejects zero/negative quantity; `Order.markPaid()` allowed only after `total > 0`.
3. **Healthcare**: `PatientRecord.addAllergy()` ensures duplicates aren’t added and audit trail is appended atomically.
4. **Messaging**: `EmailMessage.schedule(sendAt)` validates future time; content locked after `SENT` state.

---

## Design rules (quick checklist)

* Fields `private`; **no public mutable fields**.
* Prefer **methods that express intent** over generic setters: `withdraw()`, not `setBalance()`.
* Validate **once at construction**; keep invariants preserved on every mutation.
* **Return read-only views** of collections; never expose internals.
* Use **Value Objects** (immutable) for money, email, phone, etc.
* Throw **domain-specific unchecked exceptions** (e.g., `InsufficientBalanceException`).
* Keep constructors minimal; use **static factories/builders** to enforce rules.
* Keep API surface **narrow**; expose only what clients truly need.

---

## Tips & tricks (battle-tested)

* **Make illegal states unrepresentable**: e.g., `Money` cannot be negative; `Email` can’t be malformed.
* **Immutability first** for value types; it massively reduces bugs and simplifies concurrency.
* **Defensive copies** for incoming/outgoing mutable inputs (lists, dates).
* **Package-private constructors** + factories: callers can’t bypass validation.
* **Method naming that states policy**: `reserve(amount)` vs `setReserved(amount)`.
* **Encapsulate time & randomness** behind interfaces (e.g., `Clock`, `IdGenerator`) for testability.

---

## Common bad uses / error-prone patterns

* **Anemic Domain Model**: classes with fields + getters/setters only; rules live in services → invariants scatter and leak.
* **All-args setters**: external code can break invariants (`setStatus(SHIPPED)` before `PAID`).
* **Exposing mutable collections**: returning a `List` that callers can modify directly.
* **Leaking internal IDs/tokens**: e.g., exposing `paymentProviderToken` outside the aggregate boundary.
* **“Lombok @Data” everywhere**: generates setters for everything; easy to mutate illegally. Prefer `@Getter` + custom methods.
* **DTOs used as Entities**: UI shapes creeping into domain; violates encapsulation and couples domain to presentation.
* **Concurrency foot-gun**: sharing mutable objects across threads without confinement or immutability.

---

## Concurrency notes (enterprise reality)

* Favor **immutable value objects** (`Money`, `Email`).
* If mutation is required, **confine** it: only mutate within a synchronized/locked method and **never leak references** to mutable internals.
* Use a **private lock object** (`private final Object lock = new Object();`) rather than locking on `this`.

---

## Testing strategy (how you know you did it right)

* **Invariants tests**: constructing or mutating with illegal inputs throws domain exceptions.
* **State transition tests**: impossible transitions (e.g., `CANCELLED → SHIPPED`) must fail.
* **Property-based tests** for value objects: amounts remain non-negative; addition/subtraction obeys algebraic properties.
* **Mutation tests** (if available): ensure tests fail when rules are removed.

---

## Micro-exercises (do these before we code)

1. **Encapsulate Money**
   Design a `Money` value object that can’t be negative, supports add/subtract, and normalizes scale (e.g., ₹10.0 == ₹10.00). Identify where to validate.
2. **Guarded Order**
   Create `Order` with states: `CREATED → PAID → FULFILLED`. Expose methods that *only* allow legal transitions. Where do you prevent `FULFILLED` before `PAID`?
3. **Safe Ledger**
   `Account.getTransactions()` should give read-only access. How will you expose history without leaking the mutable list?

---

## Interview-style questions (to self-assess)

* How does encapsulation differ from abstraction?
* Give three ways callers accidentally break encapsulation in Java code.
* Why are immutable value objects powerful for concurrency and correctness?

---

## What we’ll code next (after theory)

* A small **Banking** domain with `Money`, `Account`, and a guarded ledger.
* We’ll follow **enterprise packaging** and show **good vs bad** implementations side-by-side.

