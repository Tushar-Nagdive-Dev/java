package org.fooddeliveryx.execution;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class DeliveryStats {
    public static void main(String[] args) {
        List<DeliveryRecord> deliveries = new ArrayList<>();
        for (int i = 0; i < 10000000; i++) {
            deliveries.add(new DeliveryRecord("R" + (i % 10),"P" + (i % 5), ThreadLocalRandom.current().nextInt(20, 50)));
        }

        long start = System.currentTimeMillis();
        Map<String, Double> avgByRestaurant = deliveries.parallelStream()
                .collect(Collectors.groupingBy(DeliveryRecord::restaurant, Collectors.averagingInt(DeliveryRecord::time)));

        long end = System.currentTimeMillis();

        System.out.println("Avg time by restaurant (Parallel): " + avgByRestaurant);
        System.out.println("Took : " + (end - start) + " ms");

        start = System.currentTimeMillis();
        Map<String, Double> avgByRestaurantS = deliveries.stream()
                .collect(Collectors.groupingBy(DeliveryRecord::restaurant, Collectors.averagingInt(DeliveryRecord::time)));

        end = System.currentTimeMillis();

        System.out.println("Avg time by restaurant (Sequential): " + avgByRestaurantS);
        System.out.println("Took : " + (end - start) + " ms");
    }

    record DeliveryRecord(String restaurant, String partner, Integer time) {}
}
