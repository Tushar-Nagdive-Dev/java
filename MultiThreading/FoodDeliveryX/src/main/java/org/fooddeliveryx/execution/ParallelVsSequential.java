package org.fooddeliveryx.execution;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class ParallelVsSequential {

    // Define the size of the list to be processed.
    private static final int LIST_SIZE = 50_000_00;

    // A computationally intensive operation to simulate a heavy workload.
    // We calculate the square root of each number multiple times.
    private static double intenseCalculation(long number) {
        double result = Math.sqrt(number);
        for (int i = 0; i < 350; i++) { // Increase iterations for more intensive CPU work
            result = Math.sqrt(result);
        }
        return result;
    }

    public static void main(String[] args) {
        // Create a large list of random numbers.
        System.out.println("Generating a list of " + LIST_SIZE + " numbers...");
        List<Long> numbers = new Random().longs(LIST_SIZE, 1L, 1000L).boxed().collect(Collectors.toList());
        System.out.println("List generation complete.\n");

        // --- Sequential Processing ---
        System.out.println("Starting sequential stream processing...");
        long sequentialStartTime = System.currentTimeMillis();
        numbers.stream()
                .mapToDouble(ParallelVsSequential::intenseCalculation)
                .sum();
        long sequentialEndTime = System.currentTimeMillis();
        long sequentialTime = sequentialEndTime - sequentialStartTime;
        System.out.println("Sequential processing took: " + sequentialTime + " ms\n");

        // --- Parallel Processing ---
        System.out.println("Starting parallel stream processing...");
        long parallelStartTime = System.currentTimeMillis();
        numbers.parallelStream()
                .mapToDouble(ParallelVsSequential::intenseCalculation)
                .sum();
        long parallelEndTime = System.currentTimeMillis();
        long parallelTime = parallelEndTime - parallelStartTime;
        System.out.println("Parallel processing took: " + parallelTime + " ms\n");

        // --- Result Comparison ---
        System.out.println("--- Comparison ---");
        System.out.println("Sequential time: " + sequentialTime + " ms");
        System.out.println("Parallel time:   " + parallelTime + " ms");
        if (parallelTime < sequentialTime) {
            System.out.println("\n✅ Parallel processing was faster!");
            double speedup = (double) sequentialTime / parallelTime;
            System.out.printf("Speedup factor: %.2fx\n", speedup);
        } else {
            System.out.println("\n❌ Sequential processing was faster or similar.");
            System.out.println("This can happen if the task is too small or the overhead is too high.");
        }
    }
}
