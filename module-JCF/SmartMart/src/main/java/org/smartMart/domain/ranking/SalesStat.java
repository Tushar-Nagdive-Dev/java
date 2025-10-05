package org.smartMart.domain.ranking;

public record SalesStat(String sku, long units) implements Comparable<SalesStat> {
    public int compareTo(SalesStat o) {
        int c = Long.compare(o.units, units);
        return c != 0 ? c : sku.compareTo(o.sku);
    }
}
