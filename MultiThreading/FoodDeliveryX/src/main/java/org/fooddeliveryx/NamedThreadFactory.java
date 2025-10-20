package org.fooddeliveryx;

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
        Thread thread = new Thread(r, base+"-"+seq.getAndIncrement());
        thread.setDaemon(daemon);
        // t.setPriority(Thread.NORM_PRIORITY); // adjust if needed
        return thread;
    }
}
