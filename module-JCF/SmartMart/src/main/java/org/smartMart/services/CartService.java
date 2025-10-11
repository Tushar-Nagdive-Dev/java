package org.smartMart.services;

import org.smartMart.domain.cart.Cart;
import org.smartMart.domain.cart.CartLine;
import org.smartMart.repo.inmemory.InMemoryCatalogRepo;

import java.math.BigDecimal;
import java.util.List;

/**
 * CartService
 * Uses LinkedHashMap cart + repo map view.
 */
public final class CartService {

    private final InMemoryCatalogRepo catalog;

    private final Cart cart = new Cart();

    public CartService(InMemoryCatalogRepo catalog) {
        this.catalog = catalog;
    }

    public void add(String sku, int qty) {
        cart.add(sku, qty);
    }

    public void remove(String sku) {
        cart.remove(sku);
    }

    public BigDecimal total() {
        return cart.total(catalog.viewMap());
    }

    public List<CartLine> view() {
        return cart.view();
    }
}
