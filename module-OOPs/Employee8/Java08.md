# 📌 Java 8+ Module 1: Lambdas & Functional Interfaces

## 🔎 What is a Lambda?

A **lambda expression** is just a concise way to define an **implementation of a functional interface** (an interface with exactly **one abstract method**).

```java
// Pre-Java 8
Runnable r = new Runnable() {
    @Override
    public void run() {
        System.out.println("Hello");
    }
};

// With Lambda
Runnable r2 = () -> System.out.println("Hello");
```

So **lambda = shortcut for anonymous class with one method**.

---

## 🔑 Core Functional Interfaces (from `java.util.function`)

Java 8 gives us a standard set of functional interfaces:

* `Supplier<T>` → `T get()` (no input, returns a value)
* `Consumer<T>` → `void accept(T t)` (takes input, no return)
* `Function<T,R>` → `R apply(T t)` (maps input → output)
* `Predicate<T>` → `boolean test(T t)` (condition checks)
* `BiFunction<T,U,R>` → takes 2 inputs, returns output
* `UnaryOperator<T>` → special case of Function (T → T)
* `BinaryOperator<T>` → special case of BiFunction (T,T → T)

---

## 🏦 Banking Domain Examples

### Example 1: Using `Predicate` for Transaction Filtering

```java
Predicate<Transaction> isDebit = t -> t.getType() == TransactionType.DEBIT;
Predicate<Transaction> largeTxn = t -> t.getAmount().asBigDecimal().doubleValue() > 1000;

boolean result = isDebit.and(largeTxn).test(transaction);
```

**Use case:** Fraud detection → check if a transaction is a **large debit**.

---

### Example 2: Using `Function` for Mapping Accounts to Views

```java
Function<Account, AccountView> toView = AccountView::from;
AccountView dto = toView.apply(account);
```

**Best practice:** Instead of writing loops with manual conversions, use `Function`.

---

### Example 3: Using `Consumer` for Notifications

```java
Consumer<Account> notifyBalance = acc -> 
    System.out.println("Account " + acc.id().value() + " has balance " + acc.balance());

accounts.forEach(notifyBalance);
```

---

### Example 4: Using `Supplier` for Lazy Object Creation

```java
Supplier<AccountId> newId = AccountId::newId;
AccountId id = newId.get();
```

---

## ✅ Tips & Best Practices

* **Don’t abuse lambdas for long logic.** Keep them **1–3 lines**, else use a method reference or a named class.
* **Use method references** when the lambda just calls an existing method (`AccountView::from`).
* **Combine predicates/consumers** using `.and()`, `.or()`, `.andThen()`.
* **Keep business rules explicit.** Don’t hide critical logic inside a cryptic lambda.
* **Prefer meaningful variable names**: e.g., `isOverdraftExceeded` instead of `p`.

---

## ❌ Bad Uses (what to avoid)

* Lambdas with >5 lines → unreadable, defeats the purpose.
* Overusing anonymous lambdas for business-critical rules (hard to test & debug).
* Mixing lambdas + side effects (`map` with `println`) → breaks functional expectations.
* Nesting lambdas too deeply → spaghetti streams.

---

## 🧪 Micro-Exercises (for you to try in project)

1. Write a `Predicate<Account>` that checks if an account is overdrawn.
2. Write a `Function<Transaction, String>` that converts a transaction to a human-readable string.
3. Write a `Consumer<Account>` that prints “ALERT: low balance” if balance < ₹1000.
4. Write a `Supplier<AccountId>` that generates a new account id.

# 📚 Theory First (Step 1: Lambdas & Functional Interfaces)

We’ll go systematically, like we did with OOP:

---

## 🔎 What are Lambdas?

* **Definition**: A **lambda expression** is an anonymous function (no name, just body) that can be passed around as a value.

* **Form**:

  ```
  (parameters) -> expression
  (parameters) -> { statements; return value; }
  ```

* **Purpose**: Concise way to represent **behavior**, not just data.

* **Key Enabler**: Works only with **Functional Interfaces** (interfaces with exactly one abstract method).

---

## 🔑 Functional Interfaces

### Categories (from `java.util.function`):

1. **Supplier<T>** → no input, returns T
   Example: `Supplier<String> s = () -> "Hello";`
2. **Consumer<T>** → takes T, returns void
   Example: `Consumer<String> c = s -> System.out.println(s);`
3. **Function<T,R>** → takes T, returns R
   Example: `Function<String, Integer> f = s -> s.length();`
4. **Predicate<T>** → takes T, returns boolean
   Example: `Predicate<Integer> p = i -> i > 10;`
5. **Operators** → specializations for same input/output

    * `UnaryOperator<T>` (T → T)
    * `BinaryOperator<T>` (T,T → T)

### Key points:

* They’re **interfaces**, so they can be implemented by lambdas.
* Java 8 provides hundreds in `java.util.function`.

---

## 🔄 How Lambdas Replace Anonymous Classes

Pre-Java 8:

```java
Runnable r = new Runnable() {
    @Override
    public void run() {
        System.out.println("Task");
    }
};
```

With Lambda:

```java
Runnable r = () -> System.out.println("Task");
```

**Benefit**: Code is shorter, more expressive.

---

## 📋 Syntax Variants

* **No parameters**:
  `() -> "Hello World"`
* **Single parameter (no type)**:
  `x -> x * x`
* **Multiple parameters**:
  `(x, y) -> x + y`
* **With type declaration**:
  `(int x, int y) -> x + y`
* **Block body**:
  `(x, y) -> { System.out.println(x+y); return x+y; }`

---

## ✅ Best Practices

* **Keep lambdas short (1–3 lines)** → else extract method.
* **Use method references** when possible: `String::toUpperCase` instead of `(s) -> s.toUpperCase()`.
* **Name lambdas meaningfully** when reused: e.g., `Predicate<String> isEmail = ...`.
* **Compose functions/predicates** with `.and()`, `.or()`, `.andThen()`.
* **Prefer functional style for transformation/filtering** (not for complex domain rules).

---

## ❌ Bad Uses

* **Long lambdas (>5 lines)** → hurts readability.
* **Side effects inside lambdas in streams** (like `map` with `println`) → breaks functional purity.
* **Overusing lambdas where simple methods are clearer**.
* **Forcing lambdas for everything** (don’t rewrite simple loops just for fashion).

---

## 🧠 Deep Understanding: Lambdas = Functions as First-Class Citizens

Before Java 8, Java treated **data as first-class, behavior as second-class**.
With lambdas, behavior is **data too**:

* Pass functions as parameters.
* Return functions from methods.
* Store functions in variables.

This brings Java closer to functional languages like Scala, Kotlin, C#.

---

## 🧪 Self-Check Questions

1. What is the difference between a **functional interface** and a normal interface?
2. How do lambdas improve readability compared to anonymous inner classes?
3. When would you prefer a **method reference** over a lambda?
4. Which functional interface would you use for:

    * Checking if salary > 50k?
    * Converting `Employee → String`?
    * Printing an account?
    * Generating a UUID?

Alright, Tushar 🚀 — let’s go deeper into the **next Java 8 core feature**:

# 📚 Theory (Step 2: Streams API)

---

## 🔎 What is a Stream?

* A **Stream** is a sequence of elements that can be **processed in a functional style**.
* Streams **don’t store data** → they operate on a data source (Collection, Array, I/O channel, etc.).
* They allow you to **express transformations and queries** declaratively, instead of manually looping.

### Example:

**Before Java 8**

```java
List<String> names = Arrays.asList("Tushar", "Maya", "Alex");
List<String> upper = new ArrayList<>();
for (String n : names) {
    upper.add(n.toUpperCase());
}
```

**With Streams**

```java
List<String> upper = names.stream()
                          .map(String::toUpperCase)
                          .toList();
```

---

## 📋 Stream Operations

### 1. **Intermediate Operations** (lazy, return a Stream)

* `map(Function)` → transform elements
* `filter(Predicate)` → select elements
* `sorted(Comparator)` → order
* `distinct()` → remove duplicates
* `limit(n)` / `skip(n)` → slice
* `peek(Consumer)` → debug side effects

### 2. **Terminal Operations** (trigger execution)

* `forEach(Consumer)` → consume elements
* `collect(Collector)` → reduce into list/map/set
* `reduce(BinaryOperator)` → combine into one value
* `count()` → number of elements
* `findFirst()`, `findAny()` → return Optional
* `anyMatch()`, `allMatch()`, `noneMatch()` → boolean checks

---

## 🔑 Stream Lifecycle

1. **Source**: Collection, Array, I/O channel, `Stream.of(...)`.
2. **Pipeline of intermediate ops** (lazy, only describe transformations).
3. **Terminal op** executes and produces a result.

---

## 🏦 Enterprise Examples

### Example 1: Filtering employees by department

```java
List<Employee> itEmployees =
    employees.stream()
             .filter(e -> e.getDepartment() == Department.IT)
             .toList();
```

### Example 2: Mapping accounts to balances

```java
List<Money> balances =
    accounts.stream()
            .map(Account::balance)
            .toList();
```

### Example 3: Summing salaries (reduce)

```java
double total =
    employees.stream()
             .map(Employee::getSalary)
             .reduce(0.0, Double::sum);
```

### Example 4: Grouping by department

```java
Map<Department, List<Employee>> byDept =
    employees.stream()
             .collect(Collectors.groupingBy(Employee::getDepartment));
```

### Example 5: Finding highest-paid employee

```java
Optional<Employee> highest =
    employees.stream()
             .max(Comparator.comparing(Employee::getSalary));
```

---

## ✅ Best Practices

* **Use method references** when lambda just calls a method (`Account::balance`).
* **Keep pipelines readable**: each step on a new line.
* **Avoid side effects**: `map`/`filter` should not mutate external state.
* **Choose correct terminal ops**: don’t use `forEach` to build collections — use `collect`.
* **Parallel streams**: use only for CPU-heavy tasks on large data, not for I/O or small lists.

---

## ❌ Bad Uses

* Overusing `.forEach` for everything (not idiomatic, often indicates “old style in disguise”).
* Nesting streams deeply (use flatMap or split pipelines).
* Relying on order when using parallel streams.
* Performing stateful operations (mutating a shared list) inside a `map`/`filter`.

---

## 🧠 Deep Insights

* Streams are **lazy** → intermediate ops don’t run until a terminal op is called.
* Streams are **one-time use** → after a terminal op, you cannot reuse it.
* Streams favor **declarative code** (describe “what” not “how”).

---

## 🧪 Self-Check Questions

1. What’s the difference between `map` and `flatMap`?
   `map` and `flatMap` are methods used in functional programming to transform collections of data, but they differ in how they handle nested structures.

* **`map`** applies a function to **each element** of a collection and returns a new collection with the results. The number of elements in the output is the **same** as the number of elements in the input. If the function you apply returns a collection, `map` will produce a nested collection.
* **`flatMap`** also applies a function to each element, but it's specifically designed for functions that return a **collection or stream**. After applying the function, `flatMap` **flattens** the nested results into a single, unified collection. The number of elements in the output is often **different** from the input, as the nested collections are combined.

-----

## Key Differences

| Feature | `map` | `flatMap` |
| :--- | :--- | :--- |
| **Output Structure** | Returns a collection of transformed elements, which may be a **nested** structure. | Returns a **flattened** collection, where nested structures are combined. |
| **Input to Function** | A single element. | A single element. |
| **Output of Function** | Can be any type of value. | **Must be a collection or stream** (an iterable object). |
| **Collection Size** | Always the **same** as the input collection size. | Can be **different** (more or less) than the input collection size. |

-----

## Practical Example in Java

Imagine a list of users, where each user has a list of unique items they've purchased.

**Scenario:** Get a single, combined list of all items purchased by all users.

```java
import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

class User {
    private String name;
    private List<String> items;

    public User(String name, List<String> items) {
        this.name = name;
        this.items = items;
    }

    public List<String> getItems() {
        return items;
    }
}

public class Store {
    public static void main(String[] args) {
        List<User> users = Arrays.asList(
            new User("Alice", Arrays.asList("apple", "banana")),
            new User("Bob", Arrays.asList("grape", "orange", "apple"))
        );

        // Using map()
        // Returns: [[apple, banana], [grape, orange, apple]]
        List<List<String>> mappedItems = users.stream()
            .map(User::getItems)
            .collect(Collectors.toList());

        System.out.println("map() result: " + mappedItems);

        // Using flatMap()
        // Returns: [apple, banana, grape, orange, apple]
        List<String> flatMappedItems = users.stream()
            .flatMap(user -> user.getItems().stream())
            .collect(Collectors.toList());

        System.out.println("flatMap() result: " + flatMappedItems);
    }
}
```

In this example, `map` gives you a `List` of `List`s, maintaining the nested structure. `flatMap` takes the nested lists and "flattens" them into a single `List` of items, which is the desired output.

2. Why are intermediate operations lazy?

Intermediate operations in Java Streams are **lazy** because they are not executed until a **terminal operation** is invoked. This design allows for optimizations that are not possible with immediate execution.

Here's why this is a powerful and efficient design choice:

### 1\. Optimization and Short-Circuiting 🏎️

Lazy execution allows the stream pipeline to be optimized. The JVM can rearrange and combine multiple intermediate operations into a single, highly efficient pass over the data. This avoids creating unnecessary intermediate collections.

For example, consider the following stream:

```java
stream.filter(isAdult).map(toName).limit(5).forEach(print);
```

* **Eager execution** would first filter all elements, then map all of them, and then limit them to 5.
* **Lazy execution** processes elements one at a time. It will get one element, check if it's an adult, map it to a name, and then move to the next. The moment the `limit(5)` operation is satisfied, the stream **short-circuits** and stops processing any more elements. It will not even check the rest of the stream's elements, which can save significant processing time for large datasets.

### 2\. Infinite Streams 🌊

Lazy evaluation is what makes it possible to work with **infinite streams**. An operation like `Stream.iterate(1, i -> i + 1)` creates an infinite stream of numbers. Without laziness, this would immediately attempt to generate all numbers, leading to an `OutOfMemoryError`. Because the intermediate operations are lazy, the stream only produces elements as needed by a terminal operation that limits the output, such as `limit(100)` or `findFirst()`.

### 3\. Separation of Concerns 🧩

Lazy operations separate the **declaration** of what you want to do from the **execution** of how to do it. The intermediate operations build a chain of transformations, but they don't perform any work. The terminal operation is the signal to start the process and consume the final result. This clear separation makes code more readable and easier to reason about.
3. Which terminal operation would you use to:

    * Count employees?
    * Find max salary?
    * Create a `Map<Department, List<Employee>>`?
4. Why should you avoid side effects in streams?
5. When is a parallel stream beneficial vs harmful?

Good morning, Tushar ☀️ — nice to see you staying consistent and structured as always.
Let’s continue right from where we left off — moving to the **next Java 8 module topic: `Optional`** 🌿

---

# 📚Theory (Step 3: `Optional`)

---

## 🔎 What is `Optional`?

`Optional<T>` is a **container object** introduced in Java 8 to represent the **presence or absence of a value** without using `null`.

> Think of `Optional` as a “maybe box” — it either contains a value (`present`) or is empty (`absent`).

Before Java 8, we’d do:

```java
if (user != null && user.getAddress() != null) {
    System.out.println(user.getAddress().getCity());
}
```

With Java 8:

```java
Optional.ofNullable(user)
        .map(User::getAddress)
        .map(Address::getCity)
        .ifPresent(System.out::println);
```

Cleaner, safer, and no `NullPointerException`.

---

## 🧩 Why `Optional` Exists

1. Avoid **null checks** and `NullPointerException`s.
2. Make “missing” values **explicit** in method contracts.
3. Encourage **functional-style chaining** instead of nested `if`s.

---

## 🧱 Creating Optionals

| Use                | Example                    | Explanation                           |
| ------------------ | -------------------------- | ------------------------------------- |
| **Empty**          | `Optional.empty()`         | no value                              |
| **Non-null value** | `Optional.of("Tushar")`    | throws `NullPointerException` if null |
| **Nullable value** | `Optional.ofNullable(obj)` | wraps null safely                     |

---

## ⚙️ Accessing Values

| Method                | Behavior                                          |
| --------------------- | ------------------------------------------------- |
| `isPresent()`         | true if contains value                            |
| `get()`               | returns value, or throws `NoSuchElementException` |
| `ifPresent(Consumer)` | runs code if value exists                         |
| `orElse(T)`           | returns value or default                          |
| `orElseGet(Supplier)` | lazy version of `orElse()`                        |
| `orElseThrow()`       | throws if empty                                   |
| `map(Function)`       | transform contained value                         |
| `flatMap(Function)`   | same, but function returns Optional               |
| `filter(Predicate)`   | keeps value only if condition passes              |

---

## 🧠 Example Scenarios

### ✅ 1. Safe value retrieval

```java
Optional<String> name = Optional.ofNullable(getUserName());
String result = name.orElse("Guest");
```

### ✅ 2. Lazy default value

```java
String config = Optional.ofNullable(System.getenv("CONFIG"))
                        .orElseGet(() -> loadDefaultConfig());
```

### ✅ 3. Avoid nested `if` checks

```java
Optional.ofNullable(order)
        .map(Order::getCustomer)
        .map(Customer::getEmail)
        .ifPresent(System.out::println);
```

### ✅ 4. Filtering inside Optionals

```java
Optional.of(200)
        .filter(price -> price > 100)
        .ifPresent(p -> System.out.println("High value: " + p));
```

---

## ⚖️ `map` vs `flatMap`

* **`map`** → function returns a value.

  ```java
  Optional.of("tushar").map(String::length); // Optional<Integer>
  ```

* **`flatMap`** → function itself returns an Optional.

  ```java
  Optional.of("tushar").flatMap(UserService::findByName); // already Optional<User>
  ```

Use `flatMap` to **avoid Optional<Optional<T>>** nesting.

---

## ✅ Best Practices

1. **Don’t use `Optional.get()` directly.**
   Always prefer `orElse`, `orElseGet`, or `ifPresent`.

2. **Use `Optional` as a return type, not as a field.**

    * Good: `public Optional<User> findUser(String id)`
    * Bad: `class User { Optional<Address> address; }`

3. **Never pass `Optional` as a method parameter.**
   Use method overloading or `null` directly for optional args.

4. **Use expressive defaults**:

   ```java
   userRepo.findById(id)
           .orElseThrow(() -> new EntityNotFoundException("User not found"));
   ```

5. **Prefer method references**:

   ```java
   optional.map(String::toUpperCase);
   ```

6. **Keep chains short (≤ 3 maps)**; if it’s longer, refactor logic into a separate method.

---

## ❌ Common Misuses

| Anti-Pattern                                | Why Bad                                      | Better              |
| ------------------------------------------- | -------------------------------------------- | ------------------- |
| `Optional` field in entity                  | Adds overhead; ORM/Jackson don’t handle well | Just nullable field |
| `Optional` as argument                      | Unclear semantics                            | Use overloads       |
| Calling `get()` without check               | Defeats purpose                              | Use safe methods    |
| Wrapping non-nullable value in `ofNullable` | Hides design issues                          | Use `of()`          |

---

## 🧩 How `Optional` Helps Architecturally

* **Encapsulates nullability** — one clear API instead of dozens of null checks.
* **Improves method contracts** — caller knows something may be absent.
* **Composes well with streams** — e.g., `.flatMap(Optional::stream)` to filter out empties elegantly.
* **Enhances readability** in chained transformations.

---

## 🧪 Self-Check Questions

1. Why should `Optional` not be used for class fields?
2. What is the difference between `orElse` and `orElseGet`?
3. Why is `Optional.get()` discouraged?
4. How would you safely access `customer.getAddress().getCity()` using `Optional`?
5. When would you use `flatMap` instead of `map`?
---

# 📚 Theory (Part 4 → Method References)

---

## 🔎 What is a Method Reference?

A **method reference** is a **shorter form of a lambda** that simply calls an existing method.

**Example:**

```java
// Lambda form
list.forEach(name -> System.out.println(name));

// Method reference form
list.forEach(System.out::println);
```

### ✅ It’s just syntactic sugar

If your lambda only **calls a method**, you can replace it with a method reference.

---

## 🧩 Syntax Forms (Four main kinds)

| Type                                                   | Syntax                      | Example               | Equivalent Lambda            |
| ------------------------------------------------------ | --------------------------- | --------------------- | ---------------------------- |
| **1. Static method**                                   | `ClassName::staticMethod`   | `Math::max`           | `(a, b) -> Math.max(a, b)`   |
| **2. Instance method (specific object)**               | `instance::instanceMethod`  | `System.out::println` | `x -> System.out.println(x)` |
| **3. Instance method (of arbitrary object of a type)** | `ClassName::instanceMethod` | `String::toLowerCase` | `s -> s.toLowerCase()`       |
| **4. Constructor reference**                           | `ClassName::new`            | `ArrayList::new`      | `() -> new ArrayList<>()`    |

---

## 🧠 Key Idea

A method reference works **only when** a lambda matches the **expected functional interface signature**.

For example:

```java
Function<String, Integer> length = String::length;
```

* The method `String.length()` returns an `int`.
* The `Function` interface expects `(T) -> R`.
  ✅ Signatures match → method reference is valid.

---

## 🏦 Enterprise-Style Examples

### Example 1 — Mapping Entities to DTOs

```java
List<AccountView> views =
    accounts.stream()
            .map(AccountView::from)
            .toList();
```

`AccountView::from` replaces `(a) -> AccountView.from(a)`.

---

### Example 2 — Printing Transactions

```java
transactions.forEach(System.out::println);
```

`System.out::println` replaces `(t) -> System.out.println(t)`.

---

### Example 3 — Comparator

```java
accounts.sort(Comparator.comparing(Account::balance));
```

`Account::balance` replaces `(a) -> a.balance()`.

---

### Example 4 — Constructor References

```java
Supplier<AccountId> idSupplier = AccountId::newId;
Supplier<List<Transaction>> newLedger = ArrayList::new;
```

---

### Example 5 — Combining with Streams

```java
List<String> upper =
    names.stream()
         .map(String::toUpperCase)
         .filter(s -> s.startsWith("A"))
         .toList();
```

---

## ✅ Best Practices

* **Use when lambda just calls a method** — improves readability.
* **Don’t overuse** — if adding extra logic, use a normal lambda.
* **Mix with collectors** for clean pipelines:

  ```java
  Map<String, Long> grouped =
      employees.stream()
               .collect(Collectors.groupingBy(Employee::getDepartment, Collectors.counting()));
  ```
* **Be explicit** when types are unclear (the compiler infers context; if it fails, use a lambda).

---

## ❌ Anti-Patterns

| Bad                                                                               | Why                                         | Better                               |
| --------------------------------------------------------------------------------- | ------------------------------------------- | ------------------------------------ |
| `list.forEach(this::processAndLog)` when `processAndLog` throws checked exception | Breaks clarity                              | handle or wrap exceptions            |
| Overusing references for every method                                             | Reduces readability when logic isn’t simple | keep lambdas when adding extra logic |
| Chaining long unclear method refs                                                 | hard to debug                               | prefer descriptive functions         |

---

## 🧪 Self-Check Questions

1. When can a lambda be replaced by a method reference?
2. Difference between `String::toLowerCase` and `System.out::println` types?
3. What are the four kinds of method references?
4. Why do method references improve readability in Streams?
5. What is a constructor reference and where can you use it?

---

# 📚 Theory (Part 5 → Default & Static Methods in Interfaces)

---

## 🔎 Why Java 8 introduced them

Before Java 8, interfaces could only have:

* **public abstract methods**
* **public static final constants**

This caused a huge **backward compatibility problem** whenever a new method was added to a library interface — every implementing class broke.

👉 To solve this, Java 8 introduced:

* **Default methods** → allow behavior with implementation in interfaces.
* **Static methods** → utility methods scoped to the interface itself.

---

## 🧩 Default Methods

A **default method** provides an **implementation** in the interface itself.

```java
public interface PaymentProcessor {
    void process();

    default void validate() {
        System.out.println("Basic validation done");
    }
}
```

**Any implementing class** automatically inherits this behavior — unless it overrides it.

```java
public class CardPaymentProcessor implements PaymentProcessor {
    @Override
    public void process() {
        System.out.println("Processing card payment");
    }
}

PaymentProcessor p = new CardPaymentProcessor();
p.validate(); // ✅ works, inherited from interface
```

---

## 🧠 Purpose of Default Methods

1. **Backward compatibility**: add new behavior without breaking old implementations.
2. **Multiple inheritance of behavior**: allow shared logic between interfaces.
3. **Encapsulate minor logic** that’s common to most implementations.

---

## ⚙️ Rules of Default Methods

| Rule                                                      | Explanation                                    |
| --------------------------------------------------------- | ---------------------------------------------- |
| Must be marked `default`                                  | Must have implementation in interface          |
| Can be overridden                                         | Implementing class can provide its own version |
| Can call other interface methods                          | yes                                            |
| Cannot override `Object` methods (like `toString`)        | compiler error                                 |
| Can cause conflicts if two interfaces define same default | must be resolved explicitly                    |

---

## ⚔️ Conflict Resolution Example

When a class implements two interfaces with the same default method:

```java
interface A {
    default void log() { System.out.println("A"); }
}

interface B {
    default void log() { System.out.println("B"); }
}

class C implements A, B {
    @Override
    public void log() {
        A.super.log(); // explicitly choose which to call
    }
}
```

If you don’t override, the compiler will throw:

> “class C inherits unrelated defaults for log() from types A and B”

---

## ⚙️ Static Methods in Interfaces

Java 8 also allowed **static methods** inside interfaces (like utility helpers).

```java
public interface Validator {
    static boolean isValidEmail(String email) {
        return email != null && email.contains("@");
    }
}
```

Usage:

```java
boolean ok = Validator.isValidEmail("tushar@example.com");
```

✅ **Static methods in interfaces:**

* Belong to the interface itself, **not inherited** by implementing classes.
* Common for utility or factory methods in domain-specific interfaces.

---

## 🏦 Enterprise Example (Banking domain)

```java
public interface AccountValidator {

    default boolean isBalanceValid(Money amount) {
        return amount.asBigDecimal().signum() >= 0;
    }

    static boolean isValidAccountType(String type) {
        return type.equalsIgnoreCase("SAVINGS") || type.equalsIgnoreCase("CURRENT");
    }
}
```

Now any class implementing `AccountValidator` gets `isBalanceValid()` by default:

```java
public class SavingsAccountValidator implements AccountValidator {
    // no need to re-implement isBalanceValid()
}
```

---

## ✅ Best Practices

* **Use default methods** for *minor cross-cutting logic* (logging, validation, audit).
* Avoid putting heavy business logic inside them — that belongs in services.
* **Use static methods** for reusable, stateless helpers within the same domain interface.
* Avoid interface method bloat — each should serve one clear purpose.
* Keep behavior-specific interfaces small (Interface Segregation Principle).

---

## ❌ Anti-Patterns

| Bad Practice                                  | Why                        | Better                      |
| --------------------------------------------- | -------------------------- | --------------------------- |
| Business logic in default method              | Breaks SRP and testability | Move to service class       |
| Multiple conflicting defaults                 | Hard to maintain           | Use abstract base class     |
| Overusing static methods                      | Defeats polymorphism       | Keep only stateless helpers |
| Making default method rely on instance fields | Not allowed / design smell | Move to class level         |

---

## 🧪 Self-Check Questions

1. What’s the difference between **default** and **abstract** methods in interfaces?
2. When would you use a **static** interface method?
3. How do you resolve the “diamond problem” when two interfaces define the same default?
4. Can you override `Object` methods like `toString()` in an interface? Why not?
5. Why are default methods important for evolving APIs?

---
# 📚 Theory (Part 6 → `java.time` Date & Time API)

---

## 🔎 Why a New Date/Time API?

Before Java 8, date handling in Java was painful 😫 —

* `java.util.Date` and `Calendar` were **mutable**, not thread-safe.
* Months were **0-indexed** (January = 0 🤦‍♂️).
* No clear separation between **date-only**, **time-only**, or **timezone-aware** types.
* Common operations (add days, format, parse) were awkward and error-prone.

> Java 8 introduced the **`java.time` package** (inspired by Joda-Time)
> → clean, immutable, type-safe, and intuitive.

---

## 🧱 Core Classes Overview

| Category           | Class           | Description                                     |
| ------------------ | --------------- | ----------------------------------------------- |
| **Date-only**      | `LocalDate`     | Year, month, day (no time or timezone)          |
| **Time-only**      | `LocalTime`     | Hour, minute, second, nano (no date)            |
| **Date + Time**    | `LocalDateTime` | Combines date + time (no timezone)              |
| **With Time Zone** | `ZonedDateTime` | Full date, time, and zone info                  |
| **Instant**        | `Instant`       | Timestamp in UTC (machine time)                 |
| **Duration**       | `Duration`      | Time-based difference (hours, minutes, seconds) |
| **Period**         | `Period`        | Date-based difference (years, months, days)     |
| **Clock**          | `Clock`         | Provides current time (useful for testing)      |

---

## 🗓️ LocalDate

**Represents a date only — no time, no zone.**

```java
LocalDate today = LocalDate.now();
LocalDate independence = LocalDate.of(1947, 8, 15);
LocalDate nextWeek = today.plusWeeks(1);
```

**Useful methods:**

```java
today.getYear();
today.getMonth();
today.getDayOfWeek();
today.isLeapYear();
```

---

## 🕒 LocalTime

**Represents only time (no date).**

```java
LocalTime now = LocalTime.now();
LocalTime meeting = LocalTime.of(10, 30);
LocalTime later = meeting.plusHours(2);
```

**Comparisons:**

```java
if (LocalTime.now().isBefore(meeting)) ...
```

---

## 🗓️🕒 LocalDateTime

**Represents date and time (no timezone).**

```java
LocalDateTime now = LocalDateTime.now();
LocalDateTime future = now.plusDays(3).minusHours(5);
```

Converting:

```java
LocalDate date = now.toLocalDate();
LocalTime time = now.toLocalTime();
```

---

## 🌍 ZonedDateTime

**Represents date + time + timezone.**

```java
ZonedDateTime india = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
ZonedDateTime newYork = india.withZoneSameInstant(ZoneId.of("America/New_York"));
```

💡 Perfect for global apps (e.g., financial systems, flight scheduling, distributed logs).

---

## ⚡ Instant

**Machine timestamp (like `Date` replacement).**
Always in UTC.

```java
Instant now = Instant.now();
Instant later = now.plus(Duration.ofHours(5));
```

Convert between legacy `Date` and new API:

```java
Date legacy = Date.from(now);
Instant modern = legacy.toInstant();
```

---

## ⏳ Duration and Period

| Type         | Represents                           | Example                        |
| ------------ | ------------------------------------ | ------------------------------ |
| **Duration** | Time-based (hours, minutes, seconds) | `Duration.between(start, end)` |
| **Period**   | Date-based (years, months, days)     | `Period.between(date1, date2)` |

```java
LocalTime start = LocalTime.of(9, 0);
LocalTime end = LocalTime.of(17, 30);
Duration d = Duration.between(start, end);
System.out.println(d.toHours()); // 8
```

```java
LocalDate d1 = LocalDate.of(2024, 1, 1);
LocalDate d2 = LocalDate.of(2025, 10, 1);
Period p = Period.between(d1, d2);
System.out.println(p.getYears() + " years, " + p.getMonths() + " months");
```

---

## 🧠 Formatting & Parsing (`DateTimeFormatter`)

```java
LocalDate date = LocalDate.now();
String formatted = date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
System.out.println(formatted);
```

Parsing back:

```java
LocalDate parsed = LocalDate.parse("05-10-2025", DateTimeFormatter.ofPattern("dd-MM-yyyy"));
```

Common built-ins:

* `DateTimeFormatter.ISO_DATE`
* `DateTimeFormatter.ISO_LOCAL_DATE_TIME`
* `DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")`

---

## 🧩 Interoperability with Old API

| Old              | New             | How to convert                                                   |
| ---------------- | --------------- | ---------------------------------------------------------------- |
| `java.util.Date` | `Instant`       | `Date.from(instant)` / `date.toInstant()`                        |
| `Calendar`       | `ZonedDateTime` | `calendar.toInstant().atZone(calendar.getTimeZone().toZoneId())` |
| `TimeZone`       | `ZoneId`        | `TimeZone.getTimeZone("Asia/Kolkata").toZoneId()`                |

---

## ✅ Best Practices

1. **Use `java.time` exclusively** for new projects (avoid mixing with `Date`/`Calendar`).
2. Always store timestamps in **UTC (`Instant`)**; convert to user zones only for display.
3. Use **immutable** time types — thread-safe by design.
4. Prefer **`Period`** for date differences, **`Duration`** for time differences.
5. Use **`ZoneId`**, not raw zone strings, for reliability.
6. For testability, use `Clock` to control “now”.

---

## ❌ Common Pitfalls

| Mistake                                | Why                  | Better                                  |
| -------------------------------------- | -------------------- | --------------------------------------- |
| Mutating `Date` and reusing            | not thread-safe      | use immutable `LocalDateTime`           |
| Forgetting timezone during conversions | inconsistent results | always use `ZonedDateTime` or `Instant` |
| Mixing `LocalDateTime` and `Instant`   | different semantics  | convert explicitly                      |
| Hardcoding offsets (`+5:30`)           | brittle              | use `ZoneId.of("Asia/Kolkata")`         |

---

## 🧪 Self-Check Questions

1. Difference between `LocalDate`, `LocalDateTime`, and `ZonedDateTime`?
2. How do `Period` and `Duration` differ conceptually?
3. Why is `java.time` thread-safe?
4. How would you format and parse a date like `"04/10/2025 10:30 AM"`?
5. Why should timestamps in databases be stored in UTC?

---

## 🗺️ Roadmap Progress (Java 8 Module)

| Stage | Topic                                  | Status          |
| ----- | -------------------------------------- | --------------- |
| 1     | Lambdas & Functional Interfaces        | ✅               |
| 2     | Streams API                            | ✅               |
| 3     | Optional                               | ✅               |
| 4     | Method References                      | ✅               |
| 5     | Default & Static Methods in Interfaces | ✅               |
| 6     | **Date/Time API (`java.time`)**        | ✅ *(this step)* |

---

✅ **At this point:**
You have completed the **entire Java 8 theoretical mastery** path — the fundamentals every architect must deeply understand.
