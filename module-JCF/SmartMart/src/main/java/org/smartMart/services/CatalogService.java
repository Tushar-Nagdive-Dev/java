package org.smartMart.services;

import org.smartMart.cache.LRUCache;
import org.smartMart.domain.catalog.Product;
import org.smartMart.indexing.CatalogIndex;
import org.smartMart.ports.CatalogPort;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

/**
 * services (application/use-case layer)
 * CatalogService
 * Orchestrates repo + indexes + cache. Demonstrates Collections choreography.
 */
public final class CatalogService {

    private final CatalogPort repo;

    private final CatalogIndex index;

    private final LRUCache<String, Product> hot;

    public CatalogService(CatalogPort repo, CatalogIndex index, int hotCap) {
        this.repo = repo;
        this.index = index;
        this.hot = new LRUCache<>(hotCap);
    }

    public void upSert(Product product) {
        repo.upSert(product);
        index.index(product);
        hot.put(product.sku, product);
    }

    public Optional<Product> bySku(String sku) {
        Product fromHot = hot.get(sku);
        if(fromHot != null) return Optional.of(fromHot);
        Optional<Product> product = repo.bySku(sku);
        product.ifPresent(v -> hot.put(sku, v));
        return product;
    }

    public Set<String> searchByTag(String tag){
        return index.byTag(tag);
    }

    public Set<String> searchByPrice(BigDecimal min, BigDecimal max){
        return index.byPriceRange(min, max);
    }

}
