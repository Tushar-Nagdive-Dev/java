package org.smartMart.ports;

import org.smartMart.domain.ranking.SalesStat;

import java.util.NavigableSet;

public interface RankingPort {

    void upSert(SalesStat salesStat);

    NavigableSet<SalesStat> topN(int n);
}
