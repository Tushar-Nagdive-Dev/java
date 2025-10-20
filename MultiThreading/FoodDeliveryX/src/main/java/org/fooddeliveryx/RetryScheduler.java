package org.fooddeliveryx;

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
