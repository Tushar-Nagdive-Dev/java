package org.fooddeliveryx;

public class TimedTask implements Runnable {
    private final Runnable delegate;
    private final Metrics metrics;
    private final long enqueueAtMs;

    public TimedTask(Runnable delegate, Metrics metrics) {
        this.delegate = delegate;
        this.metrics = metrics;
        this.enqueueAtMs = System.currentTimeMillis();
    }

    @Override
    public void run() {
        long startExec = System.currentTimeMillis();
        long queueWait = startExec - enqueueAtMs;
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
