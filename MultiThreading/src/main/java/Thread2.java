public class Thread2 {

    public static void main(String[] args) {
        Thread t1 = new NewThread();

        t1.setPriority(Thread.MAX_PRIORITY);
        t1.start();
        System.out.println("Thread start : "+t1.getName()+" Priority : "+t1.getPriority());
    }

    private static class NewThread extends Thread{
        @Override
        public void run() {
            System.out.println("Thread2 start : "+this.getName());
            this.setName("Custom-Thread-1");
//            this.setPriority(Thread.MAX_PRIORITY);
        }
    }
}

