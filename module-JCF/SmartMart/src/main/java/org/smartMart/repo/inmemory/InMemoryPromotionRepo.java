package org.smartMart.repo.inmemory;

import org.smartMart.domain.promo.Promotion;

import java.time.Instant;
import java.util.Comparator;
import java.util.Optional;
import java.util.PriorityQueue;

public class InMemoryPromotionRepo {
    private final PriorityQueue<Promotion> promotions = new PriorityQueue<>(
            Comparator.comparingInt(Promotion::priority).thenComparing(Promotion::startAt)
    );

    public void add(Promotion promotion){
        promotions.add(promotion);
    }

    public Optional<Promotion> peekActive(Instant now){
        return Optional.ofNullable(promotions.peek());
    }
}
