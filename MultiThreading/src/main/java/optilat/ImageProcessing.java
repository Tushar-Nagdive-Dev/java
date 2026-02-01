package optilat;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageProcessing {
    public static final String SOURCE_FILE = "/many-flowers.jpg";
    public static final String DESTINATION_FILE = "./out/many-flowers.jpg";

    public static void main(String[] args) throws IOException, InterruptedException {
        // 1. Try loading input from resources first (Best for Gradle/IDE)
        java.net.URL resourceUrl = ImageProcessing.class.getResource("/many-flowers.jpg");
        BufferedImage originalImage;

        if (resourceUrl != null) {
            originalImage = ImageIO.read(resourceUrl);
        } else {
            // 2. Fallback to file system if resource not found
            File f = new File(SOURCE_FILE);
            if (!f.exists()) {
                System.err.println("Error: File not found at " + f.getAbsolutePath());
                System.err.println("Please ensure 'many-flowers.jpg' is in the project root or src/main/resources.");
                return;
            }
            originalImage = ImageIO.read(f);
        }

        BufferedImage bufferedImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);

        // 3. Process the image
        long start = System.currentTimeMillis();
//        reColorSingleThread(originalImage, bufferedImage);
        recolorMultiThreaded(originalImage, bufferedImage, 100);
        long end = System.currentTimeMillis();

        long duration = end - start;
        // 4. Write output (FIXED: Create the directory first)
        File output = new File(DESTINATION_FILE);

        // Ensure the "out" folder exists, otherwise ImageIO fails
        File parentDir = output.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        ImageIO.write(bufferedImage, "jpg", output);
        System.out.println("Success! Image saved to: " + output.getAbsolutePath());
        System.out.println("Total time taken: " + duration + " ms");
    }

    // Multi Thread Solution
    public static void recolorMultiThreaded(BufferedImage originalImage, BufferedImage bufferedImage, int numberOfThreads) throws InterruptedException {
        List<Thread> threads = new ArrayList<>();
        int width = originalImage.getWidth();
        int height = originalImage.getHeight() / numberOfThreads;

        for(int i = 0; i < numberOfThreads; i++) {
            final int threadMultiplier = i;
            Thread thread = new Thread(() -> {
                int leftCorner = 0;
                int topCorner = height * threadMultiplier;

                recolorImage(originalImage, bufferedImage, leftCorner, topCorner, width, height);
            });
            threads.add(thread);
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }
    }

    // Single Thread Solution
    public static void reColorSingleThread(BufferedImage originalImage, BufferedImage bufferedImage) {
        recolorImage(originalImage, bufferedImage, 0, 0, originalImage.getWidth(), originalImage.getHeight());
    }

    public static void recolorImage(BufferedImage originalImage, BufferedImage bufferedImage, int leftCorner, int topCorner, int width, int height) {
        for(int x = leftCorner; x < leftCorner + width && x < originalImage.getWidth(); x++) {
            for(int y = topCorner; y < topCorner+height && y < originalImage.getHeight(); y++) {
                recolorPixel(originalImage, bufferedImage, x, y);
            }
        }
    }

    public static void recolorPixel(BufferedImage originalImage, BufferedImage bufferedImage, int x, int y) {
        int rgb = originalImage.getRGB(x, y);

        int red = getRed(rgb);
        int green = getGreen(rgb);
        int blue = getBlue(rgb);

        int newRed;
        int newGreen;
        int newBlue;

        if(isShadowOfGrey(red, green, blue)){
            newRed = Math.min(255, red + 10);
            newGreen = Math.max(0, green - 80);
            newBlue = Math.max(0, blue - 20);
        } else {
            newRed = red;
            newGreen = green;
            newBlue = blue;
        }

        int newRBG = createRGBFromColors(newRed, newGreen, newBlue);
        setRGB(bufferedImage, x, y, newRBG);
    }

    public static void setRGB(BufferedImage image, int x, int y, int rgb) {
        image.getRaster().setDataElements(x, y, image.getColorModel().getDataElements(rgb, null));
    }

    public static boolean isShadowOfGrey(int red, int green, int blue) {
        return Math.abs(red - green) < 30 && Math.abs(red - blue) < 30 && Math.abs(green - blue) < 30;
    }
    public static int createRGBFromColors(int red, int green, int blue) {
        int rgb = 0;

        rgb |= blue;
        rgb |= green << 8;
        rgb |= red << 16;

        rgb |= 0xFF000000;
        return rgb;
    }

    public static int getRed(int rgb) {
        return (rgb & 0x00FF0000) >> 16;
    }

    public static int getGreen(int rgb) {
        return (rgb & 0x0000FF00) >> 8;
    }

    public static int getBlue(int rgb) {
        return (rgb & 0x000000FF);
    }
}
