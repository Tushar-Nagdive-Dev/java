## 🔥 Multithreading & Concurrency Mastery Roadmap

### **1. Core Thread Concepts**

* Thread lifecycle, states, daemon vs user threads
* Thread creation: `Thread`, `Runnable`, `Callable`, `Future`
* Real-world example: Background email-sending worker

### **2. Synchronization & Thread Safety**

* `synchronized`, `volatile`, intrinsic vs explicit locks
* ReentrantLock, ReadWriteLock, Condition variables
* Atomic classes (`AtomicInteger`, `AtomicReference`, etc.)
* Real-world example: Concurrent balance update in banking

### **3. Executor Framework**

* `ExecutorService`, `ThreadPoolExecutor`, `ScheduledExecutorService`
* Core pool sizing, queue types, thread factory customization
* Practical: Request processing service for SmartMart

### **4. ForkJoin Framework**

* Work stealing, RecursiveTask, RecursiveAction
* Real-world example: Parallel computation engine

### **5. CompletableFuture & Parallel Streams**

* Async pipelines, chaining, combining futures
* Exception handling in async workflows
* Real-world example: Price aggregation from multiple APIs

### **6. Concurrency Utilities**

* `CountDownLatch`, `CyclicBarrier`, `Semaphore`, `Exchanger`, `Phaser`
* Real-world example: Parallel data loader coordination

### **7. Advanced Topics**

* Thread dumps, deadlock detection, profiling concurrency
* Reactive vs multithreaded paradigms (intro to Reactor/Project Loom)

---

## 🧠 **1. What Is a Thread (in Real Terms)?**

A **thread** is the smallest independent unit of execution within a program.
It’s like a **worker** inside your process — sharing the same memory but doing tasks independently.

💡 **Analogy:**
Imagine your Spring Boot service as a restaurant 🏢.

* The **process** = the restaurant itself (with shared kitchen, tables, and stock).
* Each **thread** = a waiter serving one customer independently.
  They all share the same space but handle different requests simultaneously.

---

## ⚙️ **2. Process vs Thread**

| Concept            | Process                                 | Thread                                      |
| ------------------ | --------------------------------------- | ------------------------------------------- |
| **Memory**         | Has its own memory                      | Shares memory with other threads            |
| **Communication**  | Inter-Process Communication (slow)      | Shared memory (fast, but risk of conflicts) |
| **Failure Impact** | One process crash doesn’t affect others | One bad thread can crash the process        |
| **Example**        | Chrome tabs (each tab = process)        | Each tab’s background scripts = threads     |

---

## 🧩 **3. Thread Lifecycle (States in JVM)**

A thread goes through **5 states** during its lifecycle:

| State                 | Description                    | Trigger                                 |
| --------------------- | ------------------------------ | --------------------------------------- |
| **NEW**               | Thread created but not started | `new Thread()`                          |
| **RUNNABLE**          | Ready to run or running        | `thread.start()`                        |
| **BLOCKED / WAITING** | Waiting for lock/resource      | `wait()`, `sleep()`, or lock contention |
| **TIMED_WAITING**     | Waiting with timeout           | `sleep(1000)`, `wait(5000)`             |
| **TERMINATED**        | Execution completed            | Thread finishes `run()`                 |

🧩 **Visual Flow:**

```
NEW → RUNNABLE → WAITING/BLOCKED → RUNNABLE → TERMINATED
```

---

## 🔨 **4. Creating Threads (3 Primary Ways)**

### ✅ **1. Extending `Thread` class**

```java
class EmailThread extends Thread {
    @Override
    public void run() {
        System.out.println("Sending email in thread: " + Thread.currentThread().getName());
    }
}

public class ThreadExample1 {
    public static void main(String[] args) {
        EmailThread emailThread = new EmailThread();
        emailThread.start();  // start() → creates a new call stack
    }
}
```

🧩 **Key Points:**

* `start()` creates a new thread and calls `run()` internally.
* If you call `run()` directly → it runs in the **main thread** (no concurrency).

---

### ✅ **2. Implementing `Runnable` Interface**

```java
class EmailTask implements Runnable {
    @Override
    public void run() {
        System.out.println("Processing email in: " + Thread.currentThread().getName());
    }
}

public class ThreadExample2 {
    public static void main(String[] args) {
        Thread thread = new Thread(new EmailTask());
        thread.start();
    }
}
```

🧩 **Advantages:**

* Better for **reusability** and **inheritance** (since you’re not forced to extend Thread).
* Often used in **ExecutorService**.

---

### ✅ **3. Using `Callable` with `Future` (Return Value + Exception Handling)**

`Runnable` can’t return results or throw checked exceptions.
`Callable<V>` can — it returns a value and can throw exceptions.

```java
import java.util.concurrent.*;

public class ThreadExample3 {
    public static void main(String[] args) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Callable<String> task = () -> {
            Thread.sleep(1000);
            return "Email sent successfully by " + Thread.currentThread().getName();
        };

        Future<String> result = executor.submit(task);

        System.out.println("Main thread doing other work...");
        System.out.println("Result: " + result.get()); // blocks until result is ready

        executor.shutdown();
    }
}
```

🧩 **Key Points:**

* `submit()` accepts a `Callable` or `Runnable`.
* `Future.get()` blocks until the task completes.
* Used in enterprise async workflows (e.g., background data fetchers).

---

## 🧠 **5. Runnable vs Callable vs Future**

| Feature      | Runnable               | Callable                | Future                |
| ------------ | ---------------------- | ----------------------- | --------------------- |
| Return value | ❌ No                   | ✅ Yes                   | ✅ Holds result        |
| Exception    | ❌ Unchecked only       | ✅ Checked allowed       | ✅ Can get via `get()` |
| Used with    | Thread / Executor      | ExecutorService         | ExecutorService       |
| Example use  | Logging, notifications | Computations, API calls | Manage results        |

---

## 💼 **6. Real-World Example (E-commerce Order Processing)**

Imagine SmartMart processes an order:

1. One thread verifies stock.
2. Another thread handles payment.
3. Another sends the confirmation email.

These run **concurrently** for better throughput.

```java
ExecutorService pool = Executors.newFixedThreadPool(3);

Callable<Boolean> stockTask = () -> { /* check stock */ return true; };
Callable<Boolean> paymentTask = () -> { /* process payment */ return true; };
Callable<String> emailTask = () -> { /* send email */ return "Email sent"; };

Future<Boolean> stock = pool.submit(stockTask);
Future<Boolean> payment = pool.submit(paymentTask);
Future<String> email = pool.submit(emailTask);

if (stock.get() && payment.get()) {
    System.out.println(email.get());
}

pool.shutdown();
```

👉 This is the **foundation** of async workflows in backend systems — later, you’ll see how this pattern evolves into **CompletableFuture** and **Reactive APIs**.

---

## ⚡ Common Mistakes to Avoid

* ❌ Calling `run()` directly (no concurrency)
* ❌ Forgetting `executor.shutdown()`
* ❌ Using too many threads manually (risk: OOM or CPU starvation)
* ❌ Ignoring exceptions in background threads (use try-catch inside `run()`)

---

## ✅ **Next Lesson: Synchronization & Thread Safety**

We’ll go deep into:

* Race conditions
* `synchronized`, `volatile`
* `ReentrantLock`, `ReadWriteLock`
* Atomic variables
* Real-world: concurrent balance update in a banking app

---

## 💼 Mini Project: **BankX — Concurrent Transaction Processor**

### 🧠 Scenario

You’re designing **BankX**, a lightweight system where multiple customers deposit and withdraw money *simultaneously*.
We’ll simulate concurrent transactions using:

* Multiple **threads** for users performing deposits/withdrawals
* A shared **account balance**
* `Runnable` and `Callable` tasks to show both **fire-and-forget** and **result-based** concurrency.

This will naturally prepare us for our **next lesson** on synchronization and thread safety — because this project will deliberately cause a **race condition** that we’ll fix later. 💥

---

## 🧩 Step 1: Project Setup

Package:

```
com.bankx.concurrent
```

File:

```
BankTransactionSimulator.java
```

---

## 🧮 Step 2: Code Implementation

```java
package com.bankx.concurrent;

import java.util.concurrent.*;

public class BankTransactionSimulator {

    // Shared bank account balance
    private static double accountBalance = 1000.0;

    public static void main(String[] args) throws Exception {

        System.out.println("🏦 Initial Balance: " + accountBalance);

        ExecutorService executor = Executors.newFixedThreadPool(4);

        // Runnable: deposit money (fire and forget)
        Runnable depositTask = () -> {
            for (int i = 0; i < 3; i++) {
                double amount = 100.0;
                accountBalance += amount;
                System.out.println(Thread.currentThread().getName() + " deposited ₹" + amount +
                                   " → New Balance: " + accountBalance);
                try { Thread.sleep(500); } catch (InterruptedException ignored) {}
            }
        };

        // Runnable: withdraw money (fire and forget)
        Runnable withdrawTask = () -> {
            for (int i = 0; i < 3; i++) {
                double amount = 80.0;
                accountBalance -= amount;
                System.out.println(Thread.currentThread().getName() + " withdrew ₹" + amount +
                                   " → New Balance: " + accountBalance);
                try { Thread.sleep(400); } catch (InterruptedException ignored) {}
            }
        };

        // Callable: check final balance (returns result)
        Callable<Double> balanceCheckTask = () -> {
            Thread.sleep(1500);
            return accountBalance;
        };

        // Submit tasks
        executor.submit(depositTask);
        executor.submit(depositTask);
        executor.submit(withdrawTask);
        Future<Double> futureBalance = executor.submit(balanceCheckTask);

        System.out.println("🔁 Processing transactions concurrently...");

        // Get result from Callable
        Double finalBalance = futureBalance.get();
        System.out.println("💰 Final Balance (might be incorrect): ₹" + finalBalance);

        executor.shutdown();
        System.out.println("✅ All transactions completed.");
    }
}
```

---

### 🧠 **Expected Sample Output**

(You’ll see interleaving depending on CPU scheduling)

```
🏦 Initial Balance: 1000.0
🔁 Processing transactions concurrently...
pool-1-thread-1 deposited ₹100.0 → New Balance: 1100.0
pool-1-thread-2 withdrew ₹80.0 → New Balance: 1020.0
pool-1-thread-3 deposited ₹100.0 → New Balance: 1120.0
pool-1-thread-1 withdrew ₹80.0 → New Balance: 1040.0
pool-1-thread-2 deposited ₹100.0 → New Balance: 1140.0
pool-1-thread-3 withdrew ₹80.0 → New Balance: 1060.0
💰 Final Balance (might be incorrect): ₹1060.0
✅ All transactions completed.
```

⚠️ **But wait — sometimes the final balance won’t match the expected result!**
That’s because multiple threads are modifying `accountBalance` **without synchronization** → a **race condition** occurs. 🧨

And *that’s* exactly what we’ll fix in the next lesson.

---

### 🧠 Key Takeaways from This Exercise

| Concept                  | Explanation                                                                                       |
| ------------------------ | ------------------------------------------------------------------------------------------------- |
| **Shared Resource**      | `accountBalance` is accessed by multiple threads — unsafe.                                        |
| **Race Condition**       | Two threads update the same variable simultaneously — last one wins.                              |
| **Runnable vs Callable** | Deposits/withdrawals = `Runnable` (fire-and-forget), balance check = `Callable` (returns result). |
| **ExecutorService**      | Manages thread lifecycle efficiently — real enterprise pattern.                                   |
| **Problem Preview**      | Thread safety & synchronization required to ensure consistency.                                   |

---

### 💪 Optional Challenge

Add 5 more customers and introduce random transaction amounts using:

```java
ThreadLocalRandom.current().nextDouble(50, 150);
```

Then run it multiple times — you’ll *see different results every time!*
That’s non-deterministic thread behavior — perfect setup for synchronization.

---

# Lesson 2 — Synchronization & Thread Safety (BankX)

## 0) Why the previous code was unsafe

`accountBalance += amount;` is **not atomic**. It’s at least three steps (read → add → write). Two threads interleave and **lose updates**.

> Key rule: If multiple threads read/write the **same mutable state**, you need **coordination** (locks/atomics).

---

## 1) `synchronized`: the simplest, correct first step

### 1.1 Intrinsic lock on the account object

Create a **thread-safe** `BankAccount` and use **BigDecimal** for money (never `double` for real currency).

```java
package com.bankx.concurrent;

import java.math.BigDecimal;

public class BankAccount {
    private BigDecimal balance = new BigDecimal("1000.00");

    // Entire method is synchronized (intrinsic lock = this)
    public synchronized void deposit(BigDecimal amount) {
        balance = balance.add(amount);
    }

    public synchronized void withdraw(BigDecimal amount) {
        // example business rule
        if (balance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds");
        }
        balance = balance.subtract(amount);
    }

    public synchronized BigDecimal getBalance() {
        return balance;
    }
}
```

### 1.2 Using it in the simulator

```java
ExecutorService pool = Executors.newFixedThreadPool(4);
BankAccount account = new BankAccount();

Runnable depositor = () -> {
    for (int i = 0; i < 3; i++) {
        account.deposit(new BigDecimal("100.00"));
        System.out.println(Thread.currentThread().getName() + " deposited → " + account.getBalance());
        sleep(300);
    }
};

Runnable withdrawer = () -> {
    for (int i = 0; i < 3; i++) {
        try {
            account.withdraw(new BigDecimal("80.00"));
            System.out.println(Thread.currentThread().getName() + " withdrew → " + account.getBalance());
        } catch (Exception e) {
            System.out.println("Withdraw failed: " + e.getMessage());
        }
        sleep(250);
    }
};

pool.submit(depositor);
pool.submit(depositor);
pool.submit(withdrawer);

pool.shutdown();
```

**Takeaways**

* `synchronized` ensures **mutual exclusion** and **visibility** (happens-before).
* It’s **reentrant** (a thread can re-acquire the same lock).
* Keep critical sections **small** (lock only around shared state).

---

## 2) `synchronized` block for finer granularity

If the method has non-critical work (logging, validation), lock **only** the balance mutation:

```java
public void deposit(BigDecimal amount) {
    validate(amount);
    synchronized (this) {
        balance = balance.add(amount);
    }
    audit("deposit", amount);
}
```

---

## 3) `ReentrantLock`: more control than `synchronized`

Use when you need **tryLock**, **timeout**, **fairness**, or **multiple condition variables**.

```java
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;

public class BankAccountLocking {
    private BigDecimal balance = new BigDecimal("1000.00");
    private final Lock lock = new ReentrantLock(true); // fair lock

    public void deposit(BigDecimal amount) {
        lock.lock();
        try {
            balance = balance.add(amount);
        } finally {
            lock.unlock();
        }
    }

    public void withdraw(BigDecimal amount) {
        boolean acquired = false;
        try {
            acquired = lock.tryLock(); // or tryLock(500, TimeUnit.MILLISECONDS)
            if (!acquired) throw new IllegalStateException("System busy, try again");
            if (balance.compareTo(amount) < 0) {
                throw new IllegalStateException("Insufficient funds");
            }
            balance = balance.subtract(amount);
        } finally {
            if (acquired) lock.unlock();
        }
    }

    public BigDecimal getBalance() {
        lock.lock();
        try { return balance; } finally { lock.unlock(); }
    }
}
```

**When to prefer `ReentrantLock`**

* You need **non-blocking attempts** (`tryLock`).
* You must avoid **priority inversion** or favor fairness.
* You need **multiple conditions** (e.g., `notEmpty`, `notFull`).

---

## 4) Read-heavy patterns: `ReadWriteLock`

For scenarios with many readers and few writers:

```java
import java.util.concurrent.locks.*;

public class BankLedger {
    private BigDecimal balance = new BigDecimal("1000.00");
    private final ReadWriteLock rw = new ReentrantReadWriteLock();
    private final Lock r = rw.readLock();
    private final Lock w = rw.writeLock();

    public void deposit(BigDecimal amount) { w.lock(); try { balance = balance.add(amount); } finally { w.unlock(); } }
    public void withdraw(BigDecimal amount) { w.lock(); try { balance = balance.subtract(amount); } finally { w.unlock(); } }
    public BigDecimal getBalance() { r.lock(); try { return balance; } finally { r.unlock(); } }
}
```

---

## 5) Atomics (`Atomic*`, `LongAdder`) — when they fit

* Great for **counters**, **stats**, and **high-contention increments**.
* Not ideal for **composed operations** (check-then-act) without extra coordination.

```java
import java.util.concurrent.atomic.AtomicLong;

public class Metrics {
    private final AtomicLong successfulTx = new AtomicLong();
    private final AtomicLong failedTx = new AtomicLong();

    public void markSuccess() { successfulTx.incrementAndGet(); }
    public void markFail() { failedTx.incrementAndGet(); }
    public long successes() { return successfulTx.get(); }
}
```

> For extreme contention, prefer `LongAdder` over `AtomicLong` (better under high write concurrency).

---

## 6) Why `volatile` is **not** a lock

* `volatile` gives **visibility**, not **atomicity**.
* `volatile int x; x++` is **not safe**.
* Use it for flags (e.g., `volatile boolean running`) or to publish immutable objects safely.

```java
private volatile boolean running = true;

public void stop() { running = false; }
public void loop() {
  while (running) { /* work */ }
}
```

---

## 7) Avoiding deadlocks (must-know for architects)

**Deadlock conditions**: mutual exclusion, hold-and-wait, no preemption, circular wait.

**Prevention strategy (simple & effective):** **Global lock ordering**
Always acquire locks in the same **global order** across the codebase.

```java
// Always lock account A before account B if A.id < B.id
public void transfer(Account from, Account to, BigDecimal amt) {
    Account first = from.id() < to.id() ? from : to;
    Account second = from.id() < to.id() ? to : from;

    synchronized (first) {
        synchronized (second) {
            from.withdraw(amt);
            to.deposit(amt);
        }
    }
}
```

---

## 8) Production checklist (Architect mindset)

* Keep shared state **small** and **well-encapsulated**.
* Prefer **immutability** wherever possible.
* Keep lock hold-time **short**; avoid I/O inside locks.
* Expose **thread-safe APIs**; hide internals.
* Monitor with **JFR**, **thread dumps**, **metrics** (queue sizes, pool saturation).
* In Spring, prefer **stateless services** + delegate to **thread-safe repositories/services**.

---

## 9) Upgraded BankX: Thread-safe version (complete)

```java
package com.bankx.concurrent;

import java.math.BigDecimal;
import java.util.concurrent.*;

public class BankXApp {
    public static void main(String[] args) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(6);
        BankAccount account = new BankAccount(); // synchronized methods

        Runnable depositor = () -> {
            for (int i = 0; i < 5; i++) {
                account.deposit(new BigDecimal("100.00"));
                log("deposit", account.getBalance());
                sleep(150);
            }
        };

        Runnable withdrawer = () -> {
            for (int i = 0; i < 5; i++) {
                try {
                    account.withdraw(new BigDecimal("80.00"));
                    log("withdraw", account.getBalance());
                } catch (Exception e) {
                    System.out.println("Withdraw failed: " + e.getMessage());
                }
                sleep(120);
            }
        };

        Callable<BigDecimal> balanceCheck = account::getBalance;

        pool.submit(depositor);
        pool.submit(depositor);
        pool.submit(withdrawer);
        pool.submit(withdrawer);

        Future<BigDecimal> finalBal = pool.submit(balanceCheck);

        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);

        System.out.println("✅ Final Balance: ₹" + finalBal.get());
    }

    private static void log(String op, BigDecimal bal) {
        System.out.println(Thread.currentThread().getName() + " " + op + " → Balance: ₹" + bal);
    }
    private static void sleep(long ms) { try { Thread.sleep(ms); } catch (InterruptedException ignored) {} }
}
```

---

## 10) Quick practice (do these now)

1. **Switch** from `synchronized` to `ReentrantLock` with `tryLock(100ms)` in `withdraw()`. Log a friendly “System busy, please retry.”
2. Add **metrics** using `LongAdder` for `totalDeposits` and `totalWithdrawals`.
3. Implement a **transfer** between two accounts with **global lock ordering** to avoid deadlock.
4. Add a **read-heavy** `getStatement()` using `ReadWriteLock` (fake returning a string or snapshot).

---

## 🧭 Goal

You’ll learn to:

* Compose dependent and parallel async tasks (`thenApply`, `thenCombine`, `allOf`)
* Handle exceptions gracefully
* Add timeouts and fallbacks
* Continue using your existing timing/metrics mindset
* Understand when **CompletableFuture** beats thread pools directly

---

## 1️⃣ Why CompletableFuture?

`ExecutorService` gives control of *threads*, but you still write blocking code (`future.get()`).
`CompletableFuture` is:

* **Non-blocking** → tasks complete independently; combine results later
* **Functional-style composition** → chain transformations & combine results
* **Lightweight** → uses common ForkJoinPool (or your own)

> Think of it as a **promise**: *“When this task finishes, run these follow-ups.”*

---

## 2️⃣ FoodDeliveryX Scenario

When a customer orders food:

1. We fetch **restaurant prep time** (I/O)
2. We fetch **traffic ETA** (API)
3. We fetch **delivery-partner availability** (DB)
4. When all are ready → compute **estimated delivery time (EDT)**
5. Log the total async duration

---

## 3️⃣ Implementation — Async Composition Example

```java
package com.fooddelivery.concurrent.async;

import java.util.concurrent.*;
import java.util.function.Supplier;

public class DeliveryEstimateService {

    private static final ExecutorService exec =
        Executors.newFixedThreadPool(4, r -> new Thread(r, "cf-" + r.hashCode()));

    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();

        CompletableFuture<Integer> prepTime =
            CompletableFuture.supplyAsync(fetchPrepTime(), exec);

        CompletableFuture<Integer> trafficTime =
            CompletableFuture.supplyAsync(fetchTrafficETA(), exec);

        CompletableFuture<Integer> partnerAvailability =
            CompletableFuture.supplyAsync(fetchPartnerAvailability(), exec);

        // Combine all results when ready
        CompletableFuture<Integer> totalETA = prepTime
            .thenCombine(trafficTime, Integer::sum)
            .thenCombine(partnerAvailability, Integer::sum)
            .orTimeout(2, TimeUnit.SECONDS) // safeguard
            .exceptionally(ex -> {
                System.out.println("⚠️  Timeout or failure: " + ex.getMessage());
                return 999; // fallback ETA
            });

        System.out.println("🕓 Doing other work while async tasks run...");
        System.out.println("📦 Estimated Delivery Time: " + totalETA.get() + " mins");

        System.out.println("✅ Completed in " + (System.currentTimeMillis() - start) + " ms");
        exec.shutdown();
    }

    // Simulated async suppliers
    private static Supplier<Integer> fetchPrepTime() {
        return () -> simulate("Restaurant prep", 500, 900);
    }
    private static Supplier<Integer> fetchTrafficETA() {
        return () -> simulate("Traffic API", 300, 800);
    }
    private static Supplier<Integer> fetchPartnerAvailability() {
        return () -> simulate("Partner DB", 400, 700);
    }

    private static int simulate(String name, int min, int max) {
        int val = ThreadLocalRandom.current().nextInt(10, 30);
        int sleep = ThreadLocalRandom.current().nextInt(min, max);
        try {
            Thread.sleep(sleep);
            System.out.printf("✅ %s fetched in %d ms → %d mins%n", name, sleep, val);
        } catch (InterruptedException ignored) {}
        return val;
    }
}
```

---

### 🧩 Output Example

```
🕓 Doing other work while async tasks run...
✅ Restaurant prep fetched in 812 ms → 18 mins
✅ Partner DB fetched in 691 ms → 11 mins
✅ Traffic API fetched in 512 ms → 15 mins
📦 Estimated Delivery Time: 44 mins
✅ Completed in 843 ms
```

**Notice:**
Total elapsed ≈ largest single call (non-blocking), not the sum of all. That’s the power of parallel async composition.

---

## 4️⃣ Key APIs (with plain-English meaning)

| Method            | What it does                 | Real-world analogy                                 |
| ----------------- | ---------------------------- | -------------------------------------------------- |
| `supplyAsync()`   | Start async task with return | “Ask three teams for their reports”                |
| `thenApply()`     | Transform result             | “Convert minutes to ETA text”                      |
| `thenCombine()`   | Merge two results            | “Add prep + traffic time”                          |
| `allOf()`         | Wait for *many* futures      | “Wait for all departments before announcing total” |
| `anyOf()`         | Whichever finishes first     | “Pick the fastest courier quote”                   |
| `exceptionally()` | Handle error gracefully      | “If data missing, use default”                     |
| `orTimeout()`     | Cancel slow tasks            | “Ignore delayed API after 2 s”                     |

---

## 5️⃣ Adding Time Measurement per Stage

You can decorate each async step:

```java
CompletableFuture<Integer> prepTime =
    CompletableFuture.supplyAsync(time("Prep", fetchPrepTime()), exec);

private static <T> Supplier<T> time(String label, Supplier<T> task) {
    return () -> {
        long s = System.currentTimeMillis();
        T res = task.get();
        long dur = System.currentTimeMillis() - s;
        System.out.printf("⏱️ %s took %d ms%n", label, dur);
        return res;
    };
}
```

Now each stage reports its own duration.

---

## 6️⃣ Advanced Pattern — Async Retry

For transient network failures:

```java
private static <T> CompletableFuture<T> withRetry(Supplier<T> supplier, int maxRetries) {
    return CompletableFuture.supplyAsync(supplier)
        .handleAsync((res, ex) -> {
            if (ex == null) return CompletableFuture.completedFuture(res);
            if (maxRetries > 0) {
                System.out.println("🔁 retrying after error: " + ex.getMessage());
                return withRetry(supplier, maxRetries - 1);
            }
            throw new CompletionException(ex);
        }).thenCompose(cf -> cf);
}
```

Use:

```java
CompletableFuture<Integer> traffic =
    withRetry(fetchTrafficETA(), 2);
```

---

## 7️⃣ Architect-Level Takeaways

* ✅ **Shift from threads to flows** — think in *pipelines* of async stages.
* ✅ **Parallelism by design** — the slowest component defines total latency.
* ✅ **Functional composition** → cleaner than nested futures or blocking calls.
* ✅ **Timeouts + fallbacks** protect user experience.
* ✅ **Metrics** integrate easily (record stage durations).

---

## 8️⃣ Exercises

1. Add a **restaurant rating API** and combine it into the final ETA message.
2. Introduce a **slow API** and confirm timeout/fallback triggers.
3. Log **thread names** per stage to see ForkJoinPool behavior.
4. Swap the executor with your **deliveryPool** from Lesson 3 for unified thread management.
5. Chain a `.thenAcceptAsync()` to push the result into a Kafka topic mock (simulate event propagation).

---
# Lesson 5 — Parallel Streams & Reactive Patterns

*(with Reactor & Project Loom Introduction)*

---

## 🧭 Learning Goals

By the end of this lesson, you’ll clearly understand:

* When to use **Parallel Streams** vs **CompletableFuture**
* How to apply **functional concurrency** with minimal boilerplate
* What **Reactive Programming** is, and how frameworks like **Reactor** or **RxJava** implement it
* How **Project Loom** changes the game for concurrent architectures (Java 21+)

---

## 🍱 Scenario — FoodDeliveryX Analytics System

Let’s say you now have **thousands of completed deliveries**, and you need to compute:

1. Average delivery time
2. Most delayed restaurant
3. Top performing delivery partner

You’ll explore **three approaches** to compute these analytics concurrently:

1. Sequential Stream (baseline)
2. Parallel Stream
3. CompletableFuture Pipeline
4. Reactive Flux (intro concept)

---

## 1️⃣ Sequential Stream (baseline)

```java
List<Integer> deliveryTimes = List.of(25, 28, 32, 21, 30, 27, 33, 26);

long start = System.currentTimeMillis();
double avg = deliveryTimes.stream()
    .mapToInt(Integer::intValue)
    .average()
    .orElse(0);
long end = System.currentTimeMillis();

System.out.printf("⏱️ Sequential avg = %.2f mins (took %d ms)%n", avg, (end - start));
```

✅ Simple, readable.
❌ Processes items **one by one**, using a single thread.

---

## 2️⃣ Parallel Stream

```java
long start = System.currentTimeMillis();
double avg = deliveryTimes.parallelStream()
    .mapToInt(Integer::intValue)
    .average()
    .orElse(0);
long end = System.currentTimeMillis();

System.out.printf("⚡ Parallel avg = %.2f mins (took %d ms)%n", avg, (end - start));
```

✅ Runs across multiple threads automatically (common ForkJoinPool).
✅ Great for **CPU-bound, independent operations**.
❌ Dangerous for **I/O-heavy** or **shared-state** tasks (no order guarantee).

---

### 🧠 How it works under the hood

* Java splits the collection into chunks.
* Each chunk runs in a **ForkJoinPool** worker thread.
* Results are merged at the end.

```java
System.out.println("Threads used:");
deliveryTimes.parallelStream()
    .peek(i -> System.out.println(Thread.currentThread().getName() + " processing " + i))
    .mapToInt(Integer::intValue)
    .average()
    .orElse(0);
```

---

## 3️⃣ Parallel Stream vs CompletableFuture

| Feature         | Parallel Stream          | CompletableFuture                      |
| --------------- | ------------------------ | -------------------------------------- |
| Style           | Declarative (functional) | Imperative / pipeline                  |
| Control         | Minimal (auto splits)    | Full control (executor, retries, etc.) |
| Best for        | CPU-bound data ops       | I/O-bound async workflows              |
| Custom Executor | ❌ Hard to set            | ✅ Easy                                 |
| Error handling  | Primitive                | Robust (`exceptionally`, `handle`)     |

✅ **Architect’s rule**

* Use **Parallel Stream** for *pure data transformations* (e.g., analytics, image processing).
* Use **CompletableFuture** for *service orchestration* or *API calls*.

---

## 4️⃣ Real Example — Compute Delivery Stats Concurrently

```java
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class DeliveryStats {
    public static void main(String[] args) {
        List<DeliveryRecord> deliveries = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            deliveries.add(new DeliveryRecord("R" + (i % 10), "P" + (i % 5),
                ThreadLocalRandom.current().nextInt(20, 50)));
        }

        long start = System.currentTimeMillis();
        Map<String, Double> avgByRestaurant = deliveries.parallelStream()
            .collect(Collectors.groupingBy(
                DeliveryRecord::restaurant,
                Collectors.averagingInt(DeliveryRecord::time)
            ));
        long end = System.currentTimeMillis();

        System.out.println("📊 Avg time by restaurant (parallel): " + avgByRestaurant);
        System.out.println("⏱️ Took: " + (end - start) + " ms");
    }

    record DeliveryRecord(String restaurant, String partner, int time) {}
}
```

✅ **Auto-splits and merges** results across threads — simple but powerful.

---

## 5️⃣ Introduction to Reactive Programming (Project Reactor / RxJava)

### 🔍 Why Reactive?

Parallel Streams or Futures work fine until:

* You need *continuous, event-driven* data (not finite lists)
* You want *backpressure* (control flow between producers and consumers)
* You integrate *streaming APIs or websockets*

**Reactive Streams** handle asynchronous *data flows* with demand control.

---

### Simple example using Reactor (conceptual)

```java
import reactor.core.publisher.Flux;

Flux.just("R1", "R2", "R3")
    .map(r -> r + " → preparing")
    .delayElements(Duration.ofMillis(500))
    .subscribe(
        item -> System.out.println(Thread.currentThread().getName() + " => " + item),
        err -> System.out.println("Error: " + err),
        () -> System.out.println("✅ Stream complete")
    );
```

✅ Non-blocking
✅ Event-driven
✅ Scales to thousands of live events

---

## 6️⃣ Project Loom (Java 21+)

### 🔍 The problem:

Traditional threads are **heavy** (≈1 MB stack each). Managing thousands = expensive.

### 💡 Project Loom’s solution:

Introduces **virtual threads** — ultra-lightweight, scheduled by JVM itself.

```java
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    IntStream.range(0, 10).forEach(i ->
        executor.submit(() -> {
            System.out.println(Thread.currentThread());
            Thread.sleep(200);
            return i;
        })
    );
}
```

✅ Each task gets its own **virtual thread**
✅ No need for complex thread pools
✅ Perfect for **I/O-bound, concurrent microservices**
✅ Combine with **structured concurrency** → easier lifecycle management

---

## 7️⃣ Summary — Concurrency Choices for Architects

| Pattern             | Best For              | Pros                       | Avoid When              |
| ------------------- | --------------------- | -------------------------- | ----------------------- |
| `Thread`            | Tiny standalone jobs  | Simple                     | Not scalable            |
| `ExecutorService`   | Managed pools         | Reliable, tunable          | Complex chaining        |
| `CompletableFuture` | Async orchestration   | Functional, non-blocking   | Heavy nesting           |
| `ParallelStream`    | CPU data ops          | Clean functional syntax    | Shared state, I/O       |
| `Reactor/Rx`        | Streaming async flows | Event-driven, backpressure | Learning curve          |
| `Project Loom`      | Massive concurrency   | Simplicity, light threads  | Pre-Java21 environments |

---

## 🧠 Architect’s Takeaway

Think of concurrency as *levels of abstraction*:

1. **Threads** → “Do this simultaneously.”
2. **Executors** → “Manage these workers efficiently.”
3. **CompletableFutures** → “Run tasks asynchronously and combine results.”
4. **Parallel Streams** → “Process large data sets concurrently.”
5. **Reactive Streams** → “Respond to ongoing data flows dynamically.”
6. **Virtual Threads (Loom)** → “Make concurrency cheap and natural.”

---

## 💪 Exercise

1. Create a list of 100 restaurants → compute top 5 slowest ones using `parallelStream()`.
2. Wrap each computation in a `CompletableFuture` and compare total time vs. sequential.
3. If you have Java 21 → try replacing your `ExecutorService` with **virtual threads** and measure difference.