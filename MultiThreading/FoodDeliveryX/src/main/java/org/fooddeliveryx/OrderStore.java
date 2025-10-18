package org.fooddeliveryx;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class OrderStore {

    private final List<String> orders = new ArrayList<>(); //Order List

    private final Lock lock = new ReentrantLock();

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    private final Lock read = readWriteLock.readLock();

    private final Lock write = readWriteLock.writeLock();

    private final AtomicInteger completeCount = new AtomicInteger(0);

    private volatile boolean acceptingOrders = true;

    // 🔒 synchronized ensures only one thread adds an order at a time
    public synchronized void addOrder(String order) {
        if(!acceptingOrders) return;

        orders.add(order);
        System.out.println(Thread.currentThread().getName() + " Added order " + order);
    }

    // 🧱 ReentrantLock gives explicit control over concurrency (tryLock / unlock)
    public void pickAndDeliverOrder() {
        boolean locked = false;
        try {
            locked = lock.tryLock();
            if(locked && !orders.isEmpty()) {
                String order = orders.remove(0);
                System.out.println(Thread.currentThread().getName() + " Picked order " + order);
                Thread.sleep(300); // simulate delivery
                System.out.println(Thread.currentThread().getName() + " delivered: " + order);
                completeCount.incrementAndGet();
            }
        }catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if(locked) lock.unlock();
        }
    }

    // 📊 ReadWriteLock allows many reads but one write
    public void printReport() {
        read.lock();
        try {
            System.out.println("📊 Orders left: " + orders.size() + ", Completed: " + completeCount.get());
        } finally {
            read.unlock();
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
