package casestudy2;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Thread coordination with Thread.join()
 * Thread coordination usage
 * 1. Different threads run independently
 * 2. Order of execution out of control*/
public class Thread4 {

    public static void main(String[] args) throws InterruptedException {
        List<Long> inputNumbers = Arrays.asList(0L, 234L, 233L, 52222247L, 89711L, 7623L, 7282L, 635L);
        List<FactorialThread> factorialThreads = new ArrayList<>();
        for (Long inputNumber : inputNumbers) {
            factorialThreads.add(new FactorialThread(inputNumber));
        }

        for (FactorialThread factorialThread : factorialThreads) {
            factorialThread.setDaemon(true);
            factorialThread.start();
        }

        for (FactorialThread factorialThread : factorialThreads) {
            factorialThread.join(2000);
        }

        for (int i=0; i<inputNumbers.size(); i++) {
            FactorialThread factorialThread = factorialThreads.get(i);
            if(factorialThread.isFinished()){
                System.out.println("Factorial of "+ inputNumbers.get(i) + " is " + factorialThread.getResult());
            }else {
                System.out.println("The calculation for "+inputNumbers.get(i)+" is still in progress");
            }
        }
    }

    public static class FactorialThread extends Thread {
        private Long inputNumber;
        private BigInteger result = BigInteger.ZERO;
        private boolean isFinished = false;

        public FactorialThread(Long inputNumber) {
            this.inputNumber = inputNumber;
        }

        @Override
        public void run() {
            this.result = factorial(inputNumber);
            isFinished = true;
        }

        public BigInteger factorial(long number) {
            BigInteger tempResult = BigInteger.ONE;

            for (long i = number; i > 0; i--) {
                tempResult = tempResult.multiply(BigInteger.valueOf(i));
            }

            return tempResult;
        }

        public boolean isFinished() {
            return isFinished;
        }

        public BigInteger getResult() {
            return result;
        }
    }
}
