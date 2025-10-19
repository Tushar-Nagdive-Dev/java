package org.fooddeliveryx;

public class TimeLogger {

    // Record start time in nanoseconds for higher precision
    public static long start(String taskName) {
        long start = System.currentTimeMillis();
        System.out.println("⏱️ [" + taskName + "] started at " + start + " ms");
        return start;
    }

    // End time and print total duration
    public static void end(String taskName, long startTime) {
        long end = System.currentTimeMillis();
        long duration = end - startTime;
        System.out.println("✅ [" + taskName + "] completed in " + duration + " ms");
    }

    // Optional: returns formatted duration string
    public static String duration(long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        return duration + " ms";
    }
}
