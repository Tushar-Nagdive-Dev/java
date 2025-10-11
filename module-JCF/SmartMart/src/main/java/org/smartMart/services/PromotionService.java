package org.smartMart.services;

import org.smartMart.domain.promo.Promotion;
import org.smartMart.repo.inmemory.InMemoryPromotionRepo;

import java.time.Instant;
import java.util.Optional;

public final class PromotionService {

    private final InMemoryPromotionRepo repo;

    public PromotionService(InMemoryPromotionRepo repo){
        this.repo = repo;
    }

    public void add(Promotion p){
        repo.add(p);
    }

    public Optional<Promotion> topActive(Instant now){
        return repo.peekActive(now);
    }
}
