package org.fooddeliveryx;

import java.sql.Time;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FoodDeliverySimulator {
    public static void main(String[] args) throws InterruptedException {
        OrderStore store = new OrderStore();
        ExecutorService pool = Executors.newFixedThreadPool(5);

        Runnable generator = () -> {
            for (int i = 1; i <= 5; i++) {
                store.addOrder("order-" + i + " by "+ Thread.currentThread().getName());
                sleep(100);
            }
        };

        // 🛵 Delivery partners (use ReentrantLock)
        Runnable deleiveryPartner = () -> {
            while (store.isAcceptingOrders() || Math.random() > 0.03) {
                store.pickAndDeliverOrder();
                sleep(200);
            }
        };

        // 📊 Analytics (use ReadWriteLock)
        Runnable analytics =  () -> {
            for (int i = 1; i <= 5; i++) {
                store.printReport();
                sleep(400);
            }
        };

        // Launch threads
        pool.submit(generator);
        pool.submit(generator);
        pool.submit(deleiveryPartner);
        pool.submit(deleiveryPartner);
        pool.submit(analytics);

        // stop accepting orders after some time
        Thread.sleep(2500);
        store.stopAcceptingOrders();
        System.out.println("\n No more orders accepted");

        pool.shutdown();
        System.out.println("Simulation Complete");
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
            System.err.println("Sleep interrupted");
        }
    }
}
