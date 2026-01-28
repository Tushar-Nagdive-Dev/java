package casestudy1;

public class PoliceThread extends Thread {
    @Override
    public void run() {
        this.setName(this.getClass().getSimpleName());
        for(int i=10; i>=0;i--) {
            try{
                Thread.sleep(1000);
            }catch(InterruptedException e){
                System.out.println(e.getMessage());
            }
            System.out.println(i);
        }
        System.out.println("Capture by "+this.getName());
        System.out.println("Game over for you hackers");
        System.exit(0);
    }
}
