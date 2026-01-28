package casestudy1;

public final class Vault {
    private int password;
    public static final int MAX_PASSWORD = 999;

    public Vault(int password) {
        this.password = password;
    }

    public boolean isCorrectPassword(int guess) {
        try {
            Thread.sleep(25);
        }catch (InterruptedException e){
            System.out.println(e.getMessage());
        }
        return this.password == guess;
    }
}
