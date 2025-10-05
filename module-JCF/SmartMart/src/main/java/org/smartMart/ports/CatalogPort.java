package org.smartMart.ports;

import org.smartMart.domain.catalog.Product;

import java.util.Collection;
import java.util.Optional;

public interface CatalogPort {
    void upSert(Product product);

    Optional<Product> bySku(String sku);

    Collection<Product> all();
}
