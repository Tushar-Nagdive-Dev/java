package org.fooddeliveryx;

import java.util.concurrent.ThreadLocalRandom;

public class DeliverOrderTask implements Runnable{
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
