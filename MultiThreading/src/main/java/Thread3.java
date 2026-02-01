import java.math.BigInteger;

/**
 * Demonstrates different strategies for Thread Termination in Java.
 * <p>
 * This class covers:
 * <ul>
 * <li>Handling {@link InterruptedException} in blocking calls.</li>
 * <li>Explicitly checking the interrupt flag for long computations.</li>
 * <li>Using Daemon threads for background tasks.</li>
 * </ul>
 * * @author Tushar Nagdive
 * @version 1.1
 */
public class Thread3 {

    /**
     * Entry point of the application.
     * Starts and interrupts two different types of tasks to demonstrate termination logic.
     * * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        // Scenario 1: Interrupting a thread that is sleeping/blocking
        Thread t1 = new Thread(new BlockingTask());
        t1.setName("BlockingTask");
        t1.start();
        t1.interrupt();

        // Scenario 2: Interrupting a thread performing heavy CPU computation
        Thread t2 = new Thread(new LongComputationTask(new BigInteger("878736"), new BigInteger("10000000")));

        /* * Daemon Threads: If set to true, the JVM will exit even if this thread is still running
         * once all user threads (like main) have finished.
         */
        // t2.setDaemon(true);

        t2.setName("LongComputationTask");
        t2.start();
        t2.interrupt();
    }

    /**
     * A task that simulates a thread blocked by a long-running operation like sleep.
     * When interrupted, it catches {@link InterruptedException} to clean up.
     */
    private static class BlockingTask implements Runnable {
        @Override
        public void run() {
            try {
                // Simulate long-term blocking resource
                Thread.sleep(500000000);
            } catch (InterruptedException e) {
                System.out.println("Thread interrupted during sleep: " + Thread.currentThread().getName());
            }
        }
    }

    /**
     * A task that calculates a large power.
     * Since it's CPU-bound, it must manually poll for the interrupt signal.
     * * @param base The BigInteger to be raised to a power.
     * @param exponent The power to raise the base to.
     */
    private record LongComputationTask(BigInteger base, BigInteger exponent) implements Runnable {

        @Override
        public void run() {
            System.out.println(base + "^" + exponent + " = " + pow(base, exponent));
        }

        /**
         * Computes the power and checks for an interrupt signal in every iteration.
         * * @return The result of the calculation, or BigInteger.ZERO if interrupted.
         */
        private BigInteger pow(BigInteger base, BigInteger exponent) {
            BigInteger result = BigInteger.ONE;
            for (BigInteger i = BigInteger.ZERO; i.compareTo(exponent) <= 0; i = i.add(BigInteger.ONE)) {

                // Critical: Explicitly check for interrupt flag in long loops
                if (Thread.currentThread().isInterrupted()) {
                    System.out.println("Computation interrupted manually: " + Thread.currentThread().getName());
                    return BigInteger.ZERO;
                }
                result = result.multiply(base);
            }
            return result;
        }
    }
}