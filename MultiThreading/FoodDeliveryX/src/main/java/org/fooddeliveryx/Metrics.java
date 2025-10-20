package org.fooddeliveryx;

import java.util.concurrent.atomic.LongAdder;

public class Metrics {

    public final LongAdder submitted = new LongAdder();
    public final LongAdder completed = new LongAdder();
    public final LongAdder rejected = new LongAdder();
    public final LongAdder accepted = new LongAdder();

    public final LongAdder totalQueueWaitMs = new LongAdder();
    public final LongAdder totalExecMs = new LongAdder();

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
