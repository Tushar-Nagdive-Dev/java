package org.fooddeliveryx;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public class BackpressureRejectHandler implements RejectedExecutionHandler {
    private final Metrics metrics;

    public BackpressureRejectHandler(Metrics metrics) {
        this.metrics = metrics;
    }

    @Override
    public void rejectedExecution(Runnable run, ThreadPoolExecutor executor) {
        metrics.rejected.increment();
        // Natural backpressure: run in caller’s thread when pool is saturated
        if (!executor.isShutdown()) {
            run.run();
        }
    }
}
