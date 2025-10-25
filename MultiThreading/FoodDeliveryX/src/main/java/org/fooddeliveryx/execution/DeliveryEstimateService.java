package org.fooddeliveryx.execution;

import java.util.concurrent.*;
import java.util.function.Supplier;

public class DeliveryEstimateService {

    private static final ExecutorService executor = Executors.newFixedThreadPool(4, r -> new Thread(r, "cf-"+r.hashCode()));

    public static void main(String[] args) throws Exception{
        long start = System.currentTimeMillis();

        CompletableFuture<Integer> prepTime = CompletableFuture.supplyAsync(time("prep", fetchPrepTime()), executor);

        CompletableFuture<Integer> trafficTime = CompletableFuture.supplyAsync(time("traffic", fetchTrafficETA()), executor);

        CompletableFuture<Integer> partnerAvailibility = CompletableFuture.supplyAsync(time("partner", fetchPartnerAvailability()), executor);

        //Combine all result when ready
        CompletableFuture<Integer> totalETA = prepTime.thenCombine(trafficTime, Integer::sum)
                .thenCombine(partnerAvailibility, Integer::sum)
                .orTimeout(2, TimeUnit.SECONDS) //SafeGuard
                .exceptionally(ex -> {
                    System.out.println("TimeOut or failure: "+ ex.getMessage());
                    return 999;
                });

        System.out.println("🕓 Doing other work while async tasks run...");
        System.out.println("📦 Estimated Delivery Time: " + totalETA.get() + " mins");

        System.out.println("✅ Completed in " + (System.currentTimeMillis() - start) + " ms");

        executor.shutdown();
    }

    //simulated async suppliers
    private static Supplier<Integer> fetchPrepTime() {
        return () -> simulate("Restaurant prep", 500, 900);
    }

    private static Supplier<Integer> fetchTrafficETA() {
        return () -> simulate("Traffic ETA", 300, 800);
    }

    private static Supplier<Integer> fetchPartnerAvailability() {
        return () -> simulate("Partner DB", 400, 700);
    }

    private static Integer simulate(String name, int min, int max) {
        Integer val = ThreadLocalRandom.current().nextInt(10, 30);
        Integer sleep = ThreadLocalRandom.current().nextInt(min, max);
        try {
            Thread.sleep(sleep);
            System.out.printf("%s fetched in %d ms -> %d min%n", name, sleep, val);
        }catch (InterruptedException ignored) {}
        return val;
    }

    private static <T> Supplier<T> time(String label, Supplier<T> task) {
        return () -> {
            long s = System.currentTimeMillis();
            T res = task.get();
            long dur = System.currentTimeMillis() - s;
            System.out.printf("%s took %d ms%n", label, dur);
            return res;
        };
    }

    private static <T> CompletableFuture<T> withRetry(Supplier<T> task, Integer maxRetries) {
        return CompletableFuture.supplyAsync(task).handleAsync((res, ex) -> {
            if(ex == null) return CompletableFuture.completedFuture(res);
            if(maxRetries > 0) {
                System.out.println(" retrying after error : " + ex.getMessage());
                return withRetry(task, maxRetries - 1);
            }
            throw new CompletionException(ex);
        }).thenCompose(cf -> cf);
    }
}
