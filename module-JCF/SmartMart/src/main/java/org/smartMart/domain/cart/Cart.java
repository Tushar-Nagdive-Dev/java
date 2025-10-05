package org.smartMart.domain.cart;

import org.smartMart.domain.catalog.Product;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Aggregates: Cart, CartLine
 * Collections: LinkedHashMap<String, CartLine> to keep stable add order and fast merge.
 */
public final class Cart {

    private final Map<String, CartLine> lines = new LinkedHashMap<>();

    public void add(String sku, Integer qty) {
        lines.merge(sku, new CartLine(sku, qty), (a,b) -> new CartLine(sku, a.qty() + b.qty()));
    }

    public void remove(String sku) {
        lines.remove(sku);
    }

    public BigDecimal total(Map<String, Product> bySku) {
        BigDecimal sum = BigDecimal.ZERO;
        for (CartLine line : lines.values()) {
            sum = sum.add(bySku.get(line.sku()).price.multiply(BigDecimal.valueOf(line.qty())));
        }
        return sum;
    }

    public List<CartLine> view() {
        return List.copyOf(lines.values());
    }

}
