package org.smartMart.services;

import org.smartMart.domain.ranking.SalesStat;
import org.smartMart.ports.RankingPort;
import org.smartMart.repo.inmemory.InMemoryRankingRepo;

import java.util.NavigableSet;

public final class RankingService {
    private final InMemoryRankingRepo repo;

    public RankingService(InMemoryRankingRepo repo){
        this.repo = repo;
    }

    public void upsert(String sku, long units){
        repo.upSert(new SalesStat(sku, units));
    }

    public NavigableSet<SalesStat> topN(int n){
        return repo.topN(n);
    }
}
