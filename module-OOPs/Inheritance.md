# Inheritance — Extending Behavior & Modeling Hierarchies

## Core Idea

Inheritance is about **“is-a” relationships** — a subclass reuses fields and methods from a parent class and may override/extend them.

👉 But in **enterprise architecture**, inheritance is **rarely about code reuse**; it’s about **modeling true hierarchies in the domain**.

---

## Why Inheritance Exists

* Share common **state & behavior** across related types.
* Enforce **contracts** in base classes (abstract methods).
* Enable **polymorphism** (we’ll cover next): a `List<Shape>` can hold `Circle` or `Rectangle`.

---

## Good Use Cases (enterprise examples)

1. **Banking**

    * `Account` (abstract base)
    * `SavingsAccount`, `CurrentAccount`, `FixedDepositAccount` (concrete subtypes with rules).
2. **E-commerce**

    * `Product` (base)
    * `PhysicalProduct`, `DigitalProduct`, `SubscriptionProduct`.
3. **Messaging**

    * `Message` (base)
    * `EmailMessage`, `SmsMessage`, `PushNotificationMessage`.
4. **Healthcare**

    * `MedicalRecord` (base)
    * `InpatientRecord`, `OutpatientRecord`.

---

## Key Principles

1. **Liskov Substitution Principle (LSP)**
   Subtypes must be usable wherever the base type is expected.
   Example: If `Account` allows `withdraw`, then `FixedDepositAccount` **must not break it** (either disallow at type level, or override with strict rules).

2. **Abstract Classes** vs **Interfaces**

    * **Abstract Class**: share state + behavior, allow partial implementation.
    * **Interface**: contract-only, multiple implementations.
      👉 Rule of thumb: *“use abstract classes when you share data/logic; interfaces when you only need a contract.”*

3. **Favor Composition over Inheritance**

    * Inheritance couples child to parent changes.
    * If relationship is **“has-a”**, prefer composition.
    * Example: `Car has an Engine` → composition, not inheritance.

---

## Tips & Tricks

* Use **abstract base classes** when you need common code + contract enforcement.
* Mark base classes as `abstract` if they’re not meant to be instantiated directly.
* Keep inheritance shallow (max 2–3 levels). Deep hierarchies are brittle.
* For shared utilities, prefer **composition** or **delegation** over inheritance.
* Document **what can be overridden** (`protected` hooks) and what’s final (`final` methods).

---

## Common Bad Uses (Pitfalls)

❌ **Inheriting for code reuse only**

* Example: `class ReportGenerator extends DatabaseConnection` (bad — “is-a” is false).

❌ **Violating LSP**

* Example: `Square` inherits `Rectangle` but breaks width/height behavior.

❌ **Deep Inheritance Trees**

* Hard to follow, test, and evolve; fragile base class problem.

❌ **Exposing protected state**

* Subclasses can mutate internals of base in unsafe ways.

❌ **Misusing frameworks**

* Extending classes just because framework forces it (Spring/JPA does this sometimes).
* Better: use composition/adapters.

---

## Enterprise Pattern: Template Method

Inheritance can be powerful with the **Template Method Pattern**:

* Base class defines **algorithm skeleton**.
* Subclass overrides specific steps.

Example:

* `PaymentProcessor` defines `process()` = validate → debit → notify.
* `CardPaymentProcessor` vs `UPIPaymentProcessor` override `debit()` only.

This keeps business workflows consistent.

---

## Testing Strategy

* **Substitutability tests**: Ensure subclass works everywhere base is used.
* **Abstract test suites**: One test suite for base contract, run against all subtypes.
* **Composition alternative tests**: Try rewriting with delegation; if it’s simpler, prefer that.

---

## Micro-exercises

1. **Banking Inheritance**

    * Create `Account` (abstract) with `deposit()` and `withdraw()`.
    * Implement `SavingsAccount` (min balance rule), `CurrentAccount` (overdraft allowed).
    * Ensure LSP: both must work where `Account` is expected.

2. **E-commerce Products**

    * Base class: `Product` (id, name, price).
    * Subclasses: `PhysicalProduct` (weight, shipping), `DigitalProduct` (downloadLink).
    * Add method `getDeliveryDetails()` abstract in base, concrete in subclasses.

3. **Messaging**

    * Abstract class `Message.send()`.
    * Implement `EmailMessage` and `SmsMessage`.
    * Use **Template Method**: base handles logging, subclasses handle channel-specific send.

---

## Interview-Style Questions

* What’s the difference between abstract class and interface in Java?
* Give an example of violating LSP and how to fix it.
* Why is “favor composition over inheritance” recommended?
* How would you design `Shape` hierarchy to avoid the “Square vs Rectangle” problem?
* In real enterprise projects, where do you still use inheritance today?
