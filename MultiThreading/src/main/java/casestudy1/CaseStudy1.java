package casestudy1;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CaseStudy1 {
    public static void main(String[] args) {
        Random rand = new Random();
        Vault vault = new Vault(rand.nextInt(Vault.MAX_PASSWORD));

        List<Thread> threads = new ArrayList<Thread>();

        threads.add(new AscendingHackerThread(vault));
        threads.add(new DescendingHackerThread(vault));
        threads.add(new PoliceThread());

        for (Thread thread : threads) {
            thread.start();
        }
    }
}
