package casestudy1;

public class DescendingHackerThread extends HackerThread {

    public DescendingHackerThread(Vault vault) {
        super(vault);
    }

    @Override
    public void run() {
        for(int guess = Vault.MAX_PASSWORD; guess>=0; guess--) {
            System.out.println("Guess check by "+ this.getName() +" guess : "+guess);
            if (vault.isCorrectPassword(guess)) {
                System.out.println(this.getName()+": Guessed Password : "+guess);
                System.exit(0);
            }
        }
    }
}
