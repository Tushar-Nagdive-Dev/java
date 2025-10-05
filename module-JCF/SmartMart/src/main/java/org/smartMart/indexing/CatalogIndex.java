package org.smartMart.indexing;

import org.smartMart.domain.catalog.Product;

import java.math.BigDecimal;
import java.util.*;

/**
 * CatalogIndex for price ranges & tags: TreeMap<BigDecimal, Set<String>> + HashMap<String, Set<String>>
 */
public class CatalogIndex {

    private final NavigableMap<BigDecimal, Set<String>> priceToSkus = new TreeMap<>();

    private final Map<String, Set<String>> tagToSkus = new HashMap<>();

    public void index(Product product) {
        priceToSkus.computeIfAbsent(product.price, k -> new HashSet<>()).add(product.sku);
        for (String tag : product.tags) {
            tagToSkus.computeIfAbsent(tag, k -> new HashSet<>()).add(product.sku);
        }
    }

    public void remove(Product product) {
        Optional.ofNullable(priceToSkus.get(product.price)).ifPresent(s -> s.remove(product.sku));
        for (String tag : product.tags) {
            Optional.ofNullable(tagToSkus.get(tag)).ifPresent(s -> s.remove(product.sku));
        }
    }

    public Set<String> byPriceRange(BigDecimal min, BigDecimal max) {
        Set<String> out = new LinkedHashSet<>();
        priceToSkus.subMap(min, true, max, true).values().forEach(out::addAll);
        return out;
    }

    public Set<String> byTag(String tag) {
        return tagToSkus.getOrDefault(tag, Set.of());
    }
}
