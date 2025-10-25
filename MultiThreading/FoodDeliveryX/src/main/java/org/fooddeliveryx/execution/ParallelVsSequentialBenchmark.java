package org.fooddeliveryx.execution;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ParallelVsSequentialBenchmark {

    public static void main(String[] args) throws Exception {
        // Configurable params (can be changed by args)
        int N = args.length > 0 ? Integer.parseInt(args[0]) : 5_000_000; // number of items
        int runs = args.length > 1 ? Integer.parseInt(args[1]) : 5;      // measured runs
        int warmups = args.length > 2 ? Integer.parseInt(args[2]) : 2;   // warm-up runs
        int restaurants = args.length > 3 ? Integer.parseInt(args[3]) : 10; // buckets

        System.out.printf("Benchmark start: N=%d, runs=%d, warmups=%d, restaurants=%d%n",
                N, runs, warmups, restaurants);

        // Run primitive benchmark (recommended)
        System.out.println("\n=== Primitive IntStream Benchmark (no boxing) ===");
        runPrimitiveBenchmark(N, restaurants, warmups, runs);

        // Run object-list benchmark (may need smaller N or larger heap)
        System.out.println("\n=== Object-list Benchmark with groupingByConcurrent ===");
        int objN = Math.min(N, 2_000_000); // safety: avoid OOM by default
        System.out.printf("Note: Object-list test uses N=%d (capped for memory).%n", objN);
        runObjectListBenchmark(objN, restaurants, warmups, runs);

        System.out.println("\nBenchmark complete.");
    }

    // ---------------- Primitive benchmark ----------------
    private static void runPrimitiveBenchmark(int N, int R, int warmups, int runs) {
        // Warmups
        for (int i = 0; i < warmups; i++) {
            primitiveRun(N, R);
        }

        // Measured runs
        double[] results = new double[runs];
        for (int i = 0; i < runs; i++) {
            results[i] = primitiveRun(N, R);
            System.gc(); // optionally reduce GC noise between runs
        }
        printStats(results);
    }

    private static double primitiveRun(int N, int R) {
        // Sequential
        long t0 = System.nanoTime();
        long[] sums = new long[R];
        long[] counts = new long[R];
        for (int i = 0; i < N; i++) {
            int r = i % R;
            int time = ThreadLocalRandom.current().nextInt(20, 50);
            sums[r] += time;
            counts[r]++;
        }
        long t1 = System.nanoTime();
        double seqMs = (t1 - t0) / 1_000_000.0;

        // Parallel (IntStream + LongAdder)
        long t2 = System.nanoTime();
        LongAdder[] sumsA = new LongAdder[R];
        LongAdder[] cntA = new LongAdder[R];
        for (int i = 0; i < R; i++) { sumsA[i] = new LongAdder(); cntA[i] = new LongAdder(); }

        IntStream.range(0, N).parallel().forEach(i -> {
            int r = i % R;
            int time = ThreadLocalRandom.current().nextInt(20, 50);
            sumsA[r].add(time);
            cntA[r].increment();
        });
        long t3 = System.nanoTime();
        double parMs = (t3 - t2) / 1_000_000.0;

        // sanity check: compute one average to ensure same logic
        double seqAvgFirst = counts[0] == 0 ? 0 : (double) sums[0] / counts[0];
        double parAvgFirst = cntA[0].sum() == 0 ? 0 : (double) sumsA[0].sum() / cntA[0].sum();

        // print one-line result
        System.out.printf("Primitive: Sequential=%.3f ms, Parallel=%.3f ms, sampleAvg(seq)=%.2f, sampleAvg(par)=%.2f%n",
                seqMs, parMs, seqAvgFirst, parAvgFirst);

        return parMs; // return parallel time for stats
    }

    // ---------------- Object-list benchmark ----------------
    private static void runObjectListBenchmark(int N, int R, int warmups, int runs) {
        // Build list once (may be expensive)
        System.out.println("Building object list (this may use significant memory)...");
        List<DeliveryRecord> list = new ArrayList<>(N);
        for (int i = 0; i < N; i++) {
            list.add(new DeliveryRecord("R" + (i % R),
                    "P" + (i % 5),
                    ThreadLocalRandom.current().nextInt(20, 50)));
        }
        System.out.println("Built list of size: " + list.size());

        // Warmups
        for (int i = 0; i < warmups; i++) {
            objectListRun(list);
        }

        // Measured runs
        double[] results = new double[runs];
        for (int i = 0; i < runs; i++) {
            results[i] = objectListRun(list);
            System.gc();
        }
        printStats(results);
    }

    private static double objectListRun(List<DeliveryRecord> list) {
        // Sequential using collectors
        long t0 = System.nanoTime();
        Map<String, Double> seq = list.stream()
                .collect(Collectors.groupingBy(DeliveryRecord::restaurant, Collectors.averagingInt(DeliveryRecord::time)));
        long t1 = System.nanoTime();
        double seqMs = (t1 - t0) / 1_000_000.0;

        // Parallel using groupingByConcurrent
        long t2 = System.nanoTime();
        Map<String, Double> par = list.parallelStream()
                .collect(Collectors.groupingByConcurrent(DeliveryRecord::restaurant, Collectors.averagingInt(DeliveryRecord::time)));
        long t3 = System.nanoTime();
        double parMs = (t3 - t2) / 1_000_000.0;

        System.out.printf("Objects: Sequential=%.3f ms, Parallel=%.3f ms, mapSizes: seq=%d, par=%d%n",
                seqMs, parMs, seq.size(), par.size());

        return parMs;
    }

    // ---------------- Helpers ----------------
    private static void printStats(double[] arr) {
        double sum = 0;
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        for (double v : arr) {
            sum += v;
            min = Math.min(min, v);
            max = Math.max(max, v);
        }
        System.out.printf("Measured runs -> avg=%.3f ms, min=%.3f ms, max=%.3f ms%n", (sum / arr.length), min, max);
    }

    // ---------------- DeliveryRecord ----------------
    private static record DeliveryRecord(String restaurant, String partner, int time) {}
}
