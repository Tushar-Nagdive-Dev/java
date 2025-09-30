# Abstraction — Designing Clean, Intent-Focused Interfaces

## Core idea (in one line)

**Hide the “how,” expose the “what.”** Abstraction means presenting a **minimal, stable interface** that expresses the *intent* (business capability) while hiding implementation details (data structures, algorithms, integrations).

**Mental model:** A *contract* that clients depend on, even when the implementation evolves (database swap, provider change, optimization).

---

## Why abstraction matters in real systems

* **Change tolerance:** You can switch from in-memory to Postgres, or from SMTP to SES, without touching business code.
* **Cohesion & clarity:** Models read like the business: `PaymentAuthorizer.authorize(…)`, not “open socket → send bytes.”
* **Parallel development:** Teams code against interfaces (ports) while another team builds adapters.
* **Testability:** Fake or stub the abstractions to unit-test core logic fast and deterministically.

---

## What to abstract (and what not to)

### Abstract:

* **Capabilities** (verbs) not data shapes: `authorizePayment`, `reserveInventory`, `notifyCustomer`.
* **Integration boundaries**: external systems (payment gateways, email/SMS, KYC, maps).
* **Cross-cutting services**: clock, ID generation, crypto, feature flags.
* **Policies** that may evolve: pricing, risk scoring, routing.

### Don’t over-abstract:

* Internals that are *unlikely* to change (e.g., trivial helper with stable algorithm).
* Simple data transformations better expressed inline.
* Every class into an interface (YAGNI). Favor interfaces at **boundaries** and for **multiple implementations**.

---

## Good abstraction characteristics (checklist)

* **Verb-first** operations reflect business intent (not CRUD leaks).
* **Minimal surface area**: fewer methods, stronger contracts.
* **Clear pre/post conditions**: document invariants and failure modes.
* **Stable inputs/outputs**: value objects/DTOs, not vendor-specific types.
* **No leaky details**: call sites shouldn’t know about HTTP, SQL, retries, or pagination tokens.
* **Deterministic behavior** or well-specified non-determinism (idempotency keys, at-least-once semantics).

---

## Enterprise-grade examples (concrete)

1. **Payments**

    * **Abstract**: `PaymentAuthorizer.authorize(OrderId, Money, CardToken) -> AuthorizationResult`
    * **Hide**: gateway SDK specifics, network timeouts, 3DS flows, retries, idempotency keys.
    * **Why**: switch Stripe ⇄ Adyen without touching order domain.

2. **Notifications**

    * **Abstract**: `NotificationPort.send(NotificationMessage)`
    * **Hide**: SMTP vs SES vs Twilio; templating engine; rate-limits, exponential backoff.

3. **Inventory**

    * **Abstract**: `InventoryService.reserve(Sku, Quantity) -> ReservationId`
    * **Hide**: which warehouse, SQL schema, compensation if reservation expires.

4. **Time**

    * **Abstract**: `Clock.now()` (interface); inject `SystemClock` in prod, `FixedClock` in tests.
    * **Hide**: timezone, DST quirks, system time dependency.

5. **Persistence**

    * **Abstract** (as **port**): `AccountRepository.save(Account)`, `findById(AccountId)`
    * **Hide**: JPA vs MyBatis vs Mongo; connection pools; mapping details.

---

## Anti-patterns (error-prone “abstractions”)

* **Leaky Abstraction:** exposing `HttpResponse`/`ResultSet` from a domain API. Callers learn infrastructure they shouldn’t.
* **CRUD-obsessed domain:** rich business flows squashed into `create/update/delete`, losing invariants and intent.
* **God Interface:** dozens of unrelated methods; violates Interface Segregation (ISP). Split capabilities.
* **Anemic Ports:** interface mirrors a vendor SDK (`StripeClient.charge(...)`) rather than business verbs (`authorize`, `capture`).
* **Premature Abstraction:** adding interfaces “just in case” with a single implementation and no boundary need; adds indirection without value.

---

## Tips & tricks (from production)

* **Design from use-cases**: write 3–5 real call sites first; shape the interface to read naturally in those flows.
* **Prefer domain language**: `refund`, `capture`, `settle` over `processPayment`.
* **Model failure explicitly**: result types with status + reasons; document idempotency; use domain exceptions sparingly.
* **Keep DTOs thin and stable**: convert vendor payloads at the edge; no vendor types inside the domain.
* **Version your contracts** (if cross-team): small breaking changes ripple; prefer additive evolution.
* **Observability hooks behind the port**: metrics/logs inside the adapter; the domain shouldn’t care how.

---

## Abstraction vs Encapsulation (quick contrast)

* **Encapsulation**: Protects invariants inside a class/aggregate. *Internal correctness.*
* **Abstraction**: Defines *external* contracts that hide implementation details. *External simplicity & replaceability.*

They complement each other: encapsulated models sit behind abstracted boundaries.

---

## Concurrency & scaling notes

* **Idempotent abstractions** for network boundaries (`sendNotification` with `messageId`).
* **Back-pressure aware** APIs: return futures/promises or queueing handles instead of blocking.
* **Timeouts & retries inside adapters**; the port contract should describe behavior (e.g., “at-least-once send”).

---

## Testing strategy

* **Contract tests**: one suite for the port; run against each adapter (e.g., SES adapter, SMTP adapter) to ensure uniform behavior.
* **Fake adapters**: in-memory or deterministic fakes used by domain/service tests.
* **Chaos tests** (integration): inject timeouts, 5xx, partial failures to validate resilience without changing domain tests.

---

## Micro-exercises (do these on paper/whiteboard first)

1. **Payments Port**

    * Design `PaymentAuthorizer` with methods and result types.
    * List 5 failure modes (insufficient funds, gateway timeout, 3DS failed, duplicate request, currency mismatch).
    * Specify idempotency behavior in the *contract*, not in the caller.

2. **Notifications Port**

    * Design `NotificationPort.send(NotificationMessage)` where message can be email/SMS/push.
    * Ensure callers don’t care which channel is used; how will you choose channel? (policy inside adapter vs separate strategy?)

3. **Clock Abstraction**

    * Introduce a `Clock` interface; rewrite a business rule that depends on “now.”
    * Show how tests become deterministic with `FixedClock`.

4. **Repository Port**

    * Sketch `AccountRepository` methods minimal for your use-cases (don’t mirror CRUD).
    * Decide: return `Optional<Account>` or throw `AccountNotFound`? Document the contract.

---

## Interview-style questions (self-check)

* What’s a **leaky abstraction**? Give a concrete example from a payments or persistence boundary.
* When would you **not** create an interface in Java?
* How do you design an abstraction to be **idempotent** across retries?
* How do you keep a domain model free of vendor SDK types?
* Explain **ports & adapters (hexagonal)** in one minute, with a specific system boundary.