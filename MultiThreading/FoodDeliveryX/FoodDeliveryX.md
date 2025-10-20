## 🧩 Mini Project: **FoodDeliveryX – Real-Time Order Processing System**

### 🎯 Objective

To understand — *in plain language* — when and why each concurrency mechanism is used, through one relatable real-world flow:

> “Multiple delivery partners pick, update, and deliver orders concurrently.”

---

## 🍱 Scenario Overview

You’re designing **FoodDeliveryX**, similar to Swiggy/Zomato.

**Actors:**

* **OrderGenerator** → keeps adding new orders
* **DeliveryPartner** → multiple threads picking and updating orders
* **AnalyticsService** → reads data frequently for reports
* **MetricsTracker** → keeps count of completed orders

Each part requires a different concurrency tool — and that’s what we’ll clarify.

---

## 🧠 Step 1: The Core Model

```java
package com.fooddelivery.concurrent;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.*;

public class OrderStore {

    private final List<String> orders = new ArrayList<>(); // shared list
    private final Lock lock = new ReentrantLock();
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();
    private final AtomicInteger completedCount = new AtomicInteger(0);
    private volatile boolean acceptingOrders = true;

    // 🔒 synchronized ensures only one thread adds an order at a time
    public synchronized void addOrder(String order) {
        if (!acceptingOrders) return;
        orders.add(order);
        System.out.println(Thread.currentThread().getName() + " added: " + order);
    }

    // 🧱 ReentrantLock gives explicit control over concurrency (tryLock / unlock)
    public void pickAndDeliverOrder() {
        boolean locked = false;
        try {
            locked = lock.tryLock();
            if (locked && !orders.isEmpty()) {
                String order = orders.remove(0);
                System.out.println(Thread.currentThread().getName() + " picked: " + order);
                Thread.sleep(300); // simulate delivery
                System.out.println(Thread.currentThread().getName() + " delivered: " + order);
                completedCount.incrementAndGet(); // atomic increment
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (locked) lock.unlock();
        }
    }

    // 📊 ReadWriteLock allows many reads but one write
    public void printReport() {
        readLock.lock();
        try {
            System.out.println("📊 Orders left: " + orders.size() + 
                               ", Completed: " + completedCount.get());
        } finally {
            readLock.unlock();
        }
    }

    // 🧠 volatile ensures this flag is visible to all threads
    public void stopAcceptingOrders() {
        acceptingOrders = false;
    }

    public boolean isAcceptingOrders() {
        return acceptingOrders;
    }
}
```

---

## 🚀 Step 2: Runner Class — Simulate Everything

```java
package com.fooddelivery.concurrent;

import java.util.concurrent.*;

public class FoodDeliverySimulator {
    public static void main(String[] args) throws InterruptedException {
        OrderStore store = new OrderStore();
        ExecutorService pool = Executors.newFixedThreadPool(5);

        // 🧾 Order generators (use synchronized)
        Runnable generator = () -> {
            for (int i = 1; i <= 5; i++) {
                store.addOrder("Order-" + i + " by " + Thread.currentThread().getName());
                sleep(100);
            }
        };

        // 🛵 Delivery partners (use ReentrantLock)
        Runnable deliveryPartner = () -> {
            while (store.isAcceptingOrders() || Math.random() > 0.3) {
                store.pickAndDeliverOrder();
                sleep(200);
            }
        };

        // 📊 Analytics (use ReadWriteLock)
        Runnable analytics = () -> {
            for (int i = 0; i < 5; i++) {
                store.printReport();
                sleep(400);
            }
        };

        // Launch threads
        pool.submit(generator);
        pool.submit(generator);
        pool.submit(deliveryPartner);
        pool.submit(deliveryPartner);
        pool.submit(analytics);

        // Stop accepting orders after some time
        Thread.sleep(2500);
        store.stopAcceptingOrders();
        System.out.println("\n❌ No more new orders accepted.\n");

        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);
        System.out.println("✅ Simulation complete.");
    }

    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
```

---

## 🎯 What You’ll See in Output

```
pool-1-thread-1 added: Order-1 by pool-1-thread-1
pool-1-thread-2 added: Order-1 by pool-1-thread-2
pool-1-thread-3 picked: Order-1 by pool-1-thread-1
pool-1-thread-3 delivered: Order-1 by pool-1-thread-1
📊 Orders left: 7, Completed: 1
...
❌ No more new orders accepted.
✅ Simulation complete.
```

---

## 🧩 What Each Mechanism Solves (Simple Explanation)

| Tool            | Used In                 | Why We Use It                              | Analogy                                                 |
| --------------- | ----------------------- | ------------------------------------------ | ------------------------------------------------------- |
| `synchronized`  | `addOrder()`            | Only one thread should add at a time       | Restaurant cashier handles one order at a time          |
| `ReentrantLock` | `pickAndDeliverOrder()` | Gives control over lock timing (`tryLock`) | Delivery partner checks if order pickup counter is busy |
| `ReadWriteLock` | `printReport()`         | Many threads can read, only one writes     | Multiple analysts reading the dashboard simultaneously  |
| `AtomicInteger` | `completedCount`        | Safe increments without lock               | Counter increments for each successful delivery         |
| `volatile`      | `acceptingOrders`       | Flag visible to all threads                | “Store open/closed” sign visible to all staff           |

---

## 🧠 Architect-Level Understanding

This project teaches *when and why* each tool fits:

* **Use `synchronized`** for *simple*, low-contention, single-object operations.
* **Use `ReentrantLock`** for *complex or time-sensitive* operations needing fine control.
* **Use `ReadWriteLock`** when *reads >> writes* (reporting, dashboards).
* **Use `Atomic` types** for *frequent small counter updates*.
* **Use `volatile`** for *visibility of control flags*, not for protecting data changes.

---

## 🔍 Try Yourself

1. Remove `synchronized` from `addOrder()` → you’ll see duplicate/overlapping logs.
2. Replace `tryLock()` with `lock()` → threads block more → slower delivery.
3. Add a third delivery partner → see if contention increases or some starve.
4. Run multiple times → outputs differ (true concurrency!).

---

# Lesson 3 — Executor Framework (FoodDeliveryX at scale)

## What you’ll master (enterprise focus)

* Choosing a **thread pool type** (and why)
* **Bounded queues** & **backpressure**
* **Rejection policies** (and safe fallbacks)
* **Precise timing**: queue wait vs execution time
* **Custom ThreadFactory** (naming, daemon, priority)
* **Graceful shutdown** and **saturation metrics**
* Scheduled retries for transient failures

---

## 0) When to use an Executor (vs raw threads)

* **Raw Thread**: one-off, low volume, no control, hard to tune.
* **Executor**: pooled workers, bounded queue, metrics, controlled shutdown, predictable latency.

> Architect rule: For servers and services, **always** use a tuned `ThreadPoolExecutor` with a **bounded** queue and explicit **rejection handling**.

---

## 1) Design for FoodDeliveryX

We’ll treat **“deliver this order”** as a *task* going into a **bounded queue**:

* If too many orders arrive → **backpressure** kicks in (reject/block/defer).
* We will measure:

    * **Enqueue time (wait in queue)**
    * **Execution time (run on a worker)**
    * **Total latency (enqueue + execution)**

---

## 2) Code — Production-style Executor with Metrics

### 2.1 Utilities (metrics + timers)

```java
package com.fooddelivery.concurrent.metrics;

import java.util.concurrent.atomic.LongAdder;

public class Metrics {
    public final LongAdder submitted = new LongAdder();
    public final LongAdder accepted  = new LongAdder();
    public final LongAdder rejected  = new LongAdder();
    public final LongAdder completed = new LongAdder();

    public final LongAdder totalQueueWaitMs = new LongAdder();
    public final LongAdder totalExecMs      = new LongAdder();

    public void printSnapshot() {
        long sub = submitted.sum();
        long acc = accepted.sum();
        long rej = rejected.sum();
        long cmp = completed.sum();
        long qMs = totalQueueWaitMs.sum();
        long eMs = totalExecMs.sum();

        System.out.printf(
            "📊 Metrics => submitted=%d, accepted=%d, rejected=%d, completed=%d, " +
            "avgQueueWait=%.2f ms, avgExec=%.2f ms%n",
            sub, acc, rej, cmp,
            (cmp == 0 ? 0.0 : (double) qMs / cmp),
            (cmp == 0 ? 0.0 : (double) eMs / cmp)
        );
    }
}
```

### 2.2 Task wrapper (captures enqueue/exec timing)

```java
package com.fooddelivery.concurrent.exec;

import com.fooddelivery.concurrent.metrics.Metrics;

public class TimedTask implements Runnable {
    private final Runnable delegate;
    private final Metrics metrics;
    private final long enqueuedAtMs;

    public TimedTask(Runnable delegate, Metrics metrics) {
        this.delegate = delegate;
        this.metrics = metrics;
        this.enqueuedAtMs = System.currentTimeMillis();
    }

    @Override
    public void run() {
        long startExec = System.currentTimeMillis();
        long queueWait = startExec - enqueuedAtMs;
        metrics.totalQueueWaitMs.add(queueWait);

        try {
            delegate.run();
        } finally {
            long execMs = System.currentTimeMillis() - startExec;
            metrics.totalExecMs.add(execMs);
            metrics.completed.increment();
        }
    }
}
```

### 2.3 Custom ThreadFactory (naming + daemon)

```java
package com.fooddelivery.concurrent.exec;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {
    private final String base;
    private final AtomicInteger seq = new AtomicInteger(1);
    private final boolean daemon;

    public NamedThreadFactory(String base, boolean daemon) {
        this.base = base;
        this.daemon = daemon;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r, base + "-" + seq.getAndIncrement());
        t.setDaemon(daemon);
        // t.setPriority(Thread.NORM_PRIORITY); // adjust if needed
        return t;
    }
}
```

### 2.4 Rejection policies: what and why

* **AbortPolicy** (default): throws `RejectedExecutionException`. Good to **fail fast**.
* **CallerRunsPolicy**: run on the caller’s thread, slowing producers = **natural backpressure**.
* **DiscardPolicy**: drop silently (use sparingly).
* **DiscardOldestPolicy**: drop oldest queued job (time-critical systems).

We’ll implement a **hybrid**: prefer `CallerRuns` (backpressure), but **count** it as a rejection for visibility.

```java
package com.fooddelivery.concurrent.exec;

import com.fooddelivery.concurrent.metrics.Metrics;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public class BackpressureRejectHandler implements RejectedExecutionHandler {
    private final Metrics metrics;

    public BackpressureRejectHandler(Metrics metrics) {
        this.metrics = metrics;
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        metrics.rejected.increment();
        // Natural backpressure: run in caller’s thread when pool is saturated
        if (!executor.isShutdown()) {
            r.run();
        }
    }
}
```

### 2.5 Build the ThreadPoolExecutor (bounded queue)

```java
package com.fooddelivery.concurrent;

import com.fooddelivery.concurrent.exec.*;
import com.fooddelivery.concurrent.metrics.Metrics;

import java.util.concurrent.*;

public class FoodDeliveryExecutorFactory {

    public static ThreadPoolExecutor buildDeliveryPool(Metrics metrics) {
        int cores = Math.max(2, Runtime.getRuntime().availableProcessors() / 2);
        int max   = Math.max(cores * 2, cores + 2); // safe headroom
        int keepAliveSec = 30;

        // Bounded queue is CRITICAL to avoid unbounded memory growth
        BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(50);

        ThreadPoolExecutor tpe = new ThreadPoolExecutor(
            cores,
            max,
            keepAliveSec, TimeUnit.SECONDS,
            queue,
            new NamedThreadFactory("deliverer", false),
            new BackpressureRejectHandler(metrics)
        );
        tpe.allowCoreThreadTimeOut(false); // keep cores warm
        return tpe;
    }
}
```

---

## 3) Integrate with FoodDeliveryX (end-to-end)

### 3.1 Delivery task (with occasional transient failures)

```java
package com.fooddelivery.concurrent.tasks;

import java.util.concurrent.ThreadLocalRandom;

public class DeliverOrderTask implements Runnable {
    private final String orderId;

    public DeliverOrderTask(String orderId) {
        this.orderId = orderId;
    }

    @Override
    public void run() {
        // Simulate variable work
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(100, 450));
            // Simulate 10% transient failure
            if (ThreadLocalRandom.current().nextInt(10) == 0) {
                throw new RuntimeException("Transient delivery failure for " + orderId);
            }
            System.out.println(Thread.currentThread().getName() + " delivered " + orderId);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
```

### 3.2 Scheduled retries for transient failures

```java
package com.fooddelivery.concurrent.exec;

import com.fooddelivery.concurrent.metrics.Metrics;

import java.util.concurrent.*;

public class RetryScheduler {
    private final ScheduledExecutorService retryExec =
        Executors.newScheduledThreadPool(1, new NamedThreadFactory("retry", true));
    private final Metrics metrics;

    public RetryScheduler(Metrics metrics) {
        this.metrics = metrics;
    }

    public void scheduleRetry(Runnable task, long delayMs) {
        retryExec.schedule(task, delayMs, TimeUnit.MILLISECONDS);
    }

    public void shutdown() {
        retryExec.shutdown();
    }
}
```

### 3.3 Simulator (producer → bounded executor → metrics → retries)

```java
package com.fooddelivery.concurrent;

import com.fooddelivery.concurrent.exec.TimedTask;
import com.fooddelivery.concurrent.metrics.Metrics;
import com.fooddelivery.concurrent.tasks.DeliverOrderTask;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class FoodDeliveryX_ExecutorDemo {

    public static void main(String[] args) throws InterruptedException {
        long programStart = System.nanoTime();

        Metrics metrics = new Metrics();
        ThreadPoolExecutor deliveryPool = FoodDeliveryExecutorFactory.buildDeliveryPool(metrics);

        ScheduledExecutorService retryPool =
            Executors.newScheduledThreadPool(1, r -> new Thread(r, "retry-1"));

        AtomicInteger orderSeq = new AtomicInteger(1);

        // Producer: submit bursts of orders
        Runnable producer = () -> {
            for (int i = 0; i < 80; i++) {
                String orderId = "ORD-" + orderSeq.getAndIncrement();
                metrics.submitted.increment();

                Runnable core = () -> {
                    try {
                        new DeliverOrderTask(orderId).run();
                    } catch (RuntimeException ex) {
                        System.out.println("⚠️  " + ex.getMessage() + " -> retry in 500ms");
                        // schedule retry (1 attempt for demo)
                        retryPool.schedule(() -> {
                            try {
                                new DeliverOrderTask(orderId + "-retry").run();
                            } catch (RuntimeException ex2) {
                                System.out.println("❌ permanent failure: " + ex2.getMessage());
                            }
                        }, 500, TimeUnit.MILLISECONDS);
                    }
                };

                Runnable timed = new TimedTask(core, metrics);

                try {
                    deliveryPool.execute(timed);
                    metrics.accepted.increment();
                } catch (RejectedExecutionException rex) {
                    // Should be rare since our handler calls r.run() (backpressure on caller)
                    metrics.rejected.increment();
                    timed.run(); // fallback in caller (keeps system responsive)
                }

                // Simulate spiky input
                sleep(20);
            }
        };

        // Run producer on caller thread for clarity (could be its own thread)
        producer.run();

        // Graceful shutdown
        deliveryPool.shutdown();
        retryPool.shutdown();

        // Await termination with timeout, then force if needed
        if (!deliveryPool.awaitTermination(10, TimeUnit.SECONDS)) {
            deliveryPool.shutdownNow();
        }
        if (!retryPool.awaitTermination(5, TimeUnit.SECONDS)) {
            retryPool.shutdownNow();
        }

        // Print metrics
        metrics.printSnapshot();

        double totalMs = (System.nanoTime() - programStart) / 1_000_000.0;
        System.out.printf("⏰ Total Program Time: %.2f ms (≈ %.2f s)%n", totalMs, totalMs/1000.0);
        System.out.printf("🧵 Pool Stats => poolSize=%d, active=%d, queued=%d, completed=%d%n",
                deliveryPool.getPoolSize(),
                deliveryPool.getActiveCount(),
                deliveryPool.getQueue().size(),
                deliveryPool.getCompletedTaskCount());

        System.out.println("✅ Done.");
    }

    private static void sleep(long ms) { try { Thread.sleep(ms); } catch (InterruptedException ignored) {} }
}
```

---

## 4) Why these choices (plain language)

* **Bounded queue (`ArrayBlockingQueue`)**: prevents memory blow-up under spikes.
* **Core vs Max threads**: cores handle steady load; bursts can stretch to max temporarily.
* **`BackpressureRejectHandler`**: when saturated, the **producer slows down** (runs task itself) — protects the system.
* **TimedTask**: separates **queue wait** and **execution time** — shows where bottlenecks are.
* **Metrics**: lets you **tune** pool size and queue length with real data.
* **Scheduled retries**: isolate transient failures without crashing the pool.
* **Graceful shutdown**: avoid lost tasks or hanging threads.

---

## 5) Tuning cheatsheet (how to choose numbers)

* Start with:

    * `corePoolSize = CPUs` (or `CPUs / 2` if tasks are CPU-heavy; `CPUs * 2` if I/O-heavy)
    * `maxPoolSize = core * 2` (or core + 2 for conservative growth)
    * `queue capacity = 5x core` (adjust based on latency SLO)
* Watch metrics:

    * **High avgQueueWait** → increase cores or shrink queue (or add more instances).
    * **High avgExec** → optimize task logic or add resources.
    * **Many rejections** → producer too fast; throttle upstream or scale horizontally.

---

## 6) Graceful shutdown recipe (copy-paste ready)

```java
executor.shutdown();
if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
    executor.shutdownNow();
}
```

> Never forget: leaving pools running causes tests to hang and services to refuse redeploy cleanly.

---

## 7) Quick exercises (10–15 min)

1. **Change queue** to `LinkedBlockingQueue(50)`. Compare avgQueueWait vs `ArrayBlockingQueue`.
2. Set rejection to **AbortPolicy** and catch `RejectedExecutionException` → decide whether to **drop** or **retry later**.
3. Make tasks *I/O-heavy* by increasing `Thread.sleep(300–1000)` and observe pool behavior; increase core size to see the impact.
4. Add a periodic metrics logger (`ScheduledExecutorService`) every second.
5. Try **DiscardOldestPolicy** and see how it affects latency tail.

---

## ✅ Progress tracker

* Lesson 1: Thread basics — ✅
* Lesson 2: Synchronization & thread safety — ✅
* **Lesson 3: Executor Framework — ✅ core + metrics + backpressure**