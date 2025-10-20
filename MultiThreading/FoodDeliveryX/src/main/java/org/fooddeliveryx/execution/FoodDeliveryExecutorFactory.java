package org.fooddeliveryx.execution;

import org.fooddeliveryx.BackpressureRejectHandler;
import org.fooddeliveryx.Metrics;
import org.fooddeliveryx.NamedThreadFactory;

import java.util.concurrent.*;

public class FoodDeliveryExecutorFactory {

    public static ThreadPoolExecutor buildDeliveryPool(Metrics metrics) {
        int cores = Math.max(2, Runtime.getRuntime().availableProcessors()/2);
        int max = Math.max(cores * 2, cores + 2); //safe headroom
        int keepAliveSec = 30;

        // Bounded queue is CRITICAL to avoid unbounded memory growth
        BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(50);

        ThreadPoolExecutor tpe = new ThreadPoolExecutor(
                cores,
                max,
                keepAliveSec, TimeUnit.SECONDS,
                queue,
                new NamedThreadFactory("deliverer", false),
                new BackpressureRejectHandler(metrics)
        );
        tpe.allowCoreThreadTimeOut(false); // keep cores warm
        return tpe;
    }
}
