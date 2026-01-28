public class ThreadExecute {

    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(new Runnable() {
           @Override
           public void run() {
               System.out.println("Thread1 start : "+Thread.currentThread().getName());
               System.out.println("Thread Priority : "+ Thread.currentThread().getPriority());
               throw new RuntimeException("internal server error");
           }
        });

        t1.setPriority(Thread.MAX_PRIORITY);
        t1.setName("Thread1-name");
        System.out.println("Thread1 before start : "+Thread.currentThread().getName());
        t1.start();
        System.out.println("Thread1 after start : "+Thread.currentThread().getName());

        Thread.sleep(100);

        t1.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                System.out.println("Thread1 uncaughtException : "+Thread.currentThread().getName() +" error : "+e.getMessage());
            }
        });
    }
}
