package org.fooddeliveryx;

import java.util.concurrent.*;

public class FoodDeliverySimulator {

    public static void main(String[] args) throws InterruptedException {
        // ⏰ TOTAL PROGRAM TIMER (most important addition)
        long programStartNanos = System.nanoTime();

        long simulationStart = TimeLogger.start("Full Simulation");

        OrderStore store = new OrderStore();
        ExecutorService pool = Executors.newFixedThreadPool(5);

        // Generators (timed)
        Runnable generator = () -> {
            long t = TimeLogger.start("Order Generation - " + Thread.currentThread().getName());
            for (int i = 1; i <= 5; i++) {
                store.addOrder("Order-" + i + " by " + Thread.currentThread().getName());
                sleep(100);
            }
            TimeLogger.end("Order Generation - " + Thread.currentThread().getName(), t);
        };

        // Delivery partners (timed)
        Runnable deliveryPartner = () -> {
            long t = TimeLogger.start("Delivery Partner - " + Thread.currentThread().getName());
            while (store.isAcceptingOrders() || Math.random() > 0.3) {
                store.pickAndDeliverOrder();
                sleep(200);
            }
            TimeLogger.end("Delivery Partner - " + Thread.currentThread().getName(), t);
        };

        // Analytics (timed)
        Runnable analytics = () -> {
            long t = TimeLogger.start("Analytics - " + Thread.currentThread().getName());
            for (int i = 0; i < 5; i++) {
                store.printReport();
                sleep(400);
            }
            TimeLogger.end("Analytics - " + Thread.currentThread().getName(), t);
        };

        // Submit
        pool.submit(generator);
        pool.submit(generator);
        pool.submit(deliveryPartner);
        pool.submit(deliveryPartner);
        pool.submit(analytics);

        // Stop order intake
        Thread.sleep(2500);
        store.stopAcceptingOrders();
        System.out.println("\n❌ No more new orders accepted.\n");

        // Shutdown
        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);

        // End simulation timer
        TimeLogger.end("Full Simulation", simulationStart);

        // ⏰ TOTAL PROGRAM TIMER (nanosecond precision)
        long programEndNanos = System.nanoTime();
        double totalMs = (programEndNanos - programStartNanos) / 1_000_000.0;
        System.out.printf("%n⏰ Total Program Time: %.3f ms (≈ %.3f s)%n", totalMs, totalMs / 1000.0);

        System.out.println("✅ Simulation complete.");
    }

    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
