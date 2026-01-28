package casestudy1;

public class AscendingHackerThread extends HackerThread {

    public AscendingHackerThread(Vault vault) {
        super(vault);
    }

    @Override
    public void run() {
        for(int guess = 0; guess < Vault.MAX_PASSWORD; guess++){
            System.out.println("Guess check by "+ this.getName() +" guess : "+guess);
            if(vault.isCorrectPassword(guess)){
                System.out.println(this.getName()+": Guessed Password : "+guess);
                System.exit(0);
            }
        }
    }
}
