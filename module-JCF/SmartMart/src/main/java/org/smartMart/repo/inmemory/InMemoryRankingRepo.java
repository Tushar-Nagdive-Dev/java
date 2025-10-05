package org.smartMart.repo.inmemory;

import org.smartMart.domain.ranking.SalesStat;

import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Collections: TreeSet<SalesStat> (sorted)
 */
public final class InMemoryRankingRepo {
    private final NavigableSet<SalesStat> leaderBoard = new TreeSet<>();

    public void upSert(SalesStat salesStat){
        leaderBoard.remove(salesStat);
        leaderBoard.add(salesStat);
    }

    public NavigableSet<SalesStat> topN(int n) {
        return leaderBoard.stream().limit(n).collect(Collectors.toCollection(TreeSet::new));
    }
}
