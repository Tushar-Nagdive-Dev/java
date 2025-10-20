package org.fooddeliveryx;

import org.fooddeliveryx.execution.FoodDeliveryExecutorFactory;

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
            for (int i = 0; i < 1805; i++) {
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

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {

        }
    }
}
