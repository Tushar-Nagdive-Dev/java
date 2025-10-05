package org.smartMart.repo.inmemory;

import org.smartMart.domain.catalog.Product;
import org.smartMart.ports.CatalogPort;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Collections: HashMap<String, Product> (O(1) lookup)
 */
public final class InMemoryCatalogRepo implements CatalogPort {

    private final Map<String, Product> bySku = new HashMap<>();

    @Override
    public void upSert(Product product) {
        bySku.put(product.sku, product);
    }

    @Override
    public Optional<Product> bySku(String sku) {
        return Optional.ofNullable(bySku.get(sku));
    }

    @Override
    public Collection<Product> all() {
        return bySku.values();
    }

    // for services needing map view
    public Map<String, Product> viewMap(){
        return bySku;
    }
}
