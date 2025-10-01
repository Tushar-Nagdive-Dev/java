# Polymorphism — One Interface, Many Forms

## Core Idea

Polymorphism = **“same message, different behavior.”**
It lets us treat **different types uniformly** while allowing each to provide its own behavior.

👉 Example: `Account.withdraw(amount)` works for both `SavingsAccount` and `CurrentAccount`, but the **rule is different** (min balance vs overdraft).

---

## Types of Polymorphism in Java

1. **Compile-time (Method Overloading)**

    * Same method name, different signatures.
    * Example: `print(String)` vs `print(int)`.
    * Useful but **not true OOP polymorphism** (just syntactic sugar).

2. **Runtime (Method Overriding)** ✅ (the real deal)

    * Subclass provides a specific implementation for a base method.
    * Example: `Notification.send()` → Email vs SMS.

3. **Interface-based polymorphism**

    * Multiple implementations of an interface, injected at runtime.
    * Example: `PaymentProcessor` → StripeProcessor, PayPalProcessor.

---

## Why Polymorphism Matters in Enterprise Systems

* **Extensibility**: Add new behavior without touching existing client code.
* **Dependency Inversion**: Code to interfaces, not concrete classes.
* **Plugin architectures**: Swap implementations (DB, cache, messaging).
* **Design patterns** (Strategy, State, Command, Observer) heavily rely on polymorphism.

---

## Enterprise Examples

1. **Banking**

    * `InterestCalculator.calculate(Account)` → different strategies for `SavingsAccount` vs `FixedDepositAccount`.

2. **Payments**

    * `PaymentProcessor.process(Order)` → CreditCard, UPI, Wallet processors.

3. **Notifications**

    * `NotificationPort.send(Message)` → EmailAdapter, SMSAdapter, PushAdapter.

4. **Document Storage**

    * `DocumentRepository.store(Document)` → S3, FileSystem, Database.

---

## Tips & Tricks

* Always **program to an interface**, not to a concrete class.
* Use **dependency injection** (Spring beans) to wire the right implementation.
* Keep polymorphic hierarchies **focused** (don’t overload one interface with too many methods).
* Combine with **Strategy Pattern** to avoid giant `if-else` ladders.
* Ensure **Liskov Substitution Principle** (subtypes must behave like base type).

---

## Bad Uses / Pitfalls

❌ **Switch/if-else hell instead of polymorphism**

* Bad:

  ```java
  if (paymentType == "CARD") { ... }
  else if (paymentType == "UPI") { ... }
  ```
* Good: Inject correct `PaymentProcessor`.

❌ **Overusing inheritance when interfaces suffice**

* Leads to brittle hierarchies.

❌ **Forcing polymorphism where not needed**

* Example: making every utility class an interface with multiple impls when only one is ever needed.

❌ **Breaking substitutability**

* Example: subclass changes method contract (throws extra exceptions, weakens guarantees).

---

## Testing Strategy

* **Contract tests** for interfaces (all implementations must pass the same suite).
* **Mock/fake injections** to test higher-level services without real adapters.
* **Property-based tests** to ensure polymorphic implementations behave consistently.

---

## Micro-Exercises

1. **Banking**

    * Create an `InterestCalculator` interface.
    * Implement `SavingsInterestCalculator` (simple %), `FixedDepositCalculator` (higher %).
    * Write a method that takes `List<InterestCalculator>` and applies them polymorphically.

2. **Payments**

    * Interface `PaymentProcessor.process(Order)`.
    * Implement `CardProcessor`, `UPIProcessor`.
    * Use polymorphism to process a list of mixed orders.

3. **Notifications**

    * Base interface `Notifier.send(Message)`.
    * Implement `EmailNotifier`, `SmsNotifier`.
    * Inject proper notifier via config (simulate runtime decision).

---

## Interview-Style Questions

* What’s the difference between **overloading** and **overriding**?
* How does polymorphism enable the **Strategy Pattern**?
* Explain a situation where polymorphism replaced a giant `switch` statement.
* How does polymorphism relate to the **Open/Closed Principle**?
* In Spring Boot, how is polymorphism naturally used? (hint: beans implementing an interface).