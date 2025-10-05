package org.smartMart.domain.catalog;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Set;

public final class Product {

    public final String sku;

    public final String title;

    public final BigDecimal price;

    public final Set<String> tags;

    public Product(String sku, String title, BigDecimal price, Set<String> tags) {
        this.sku = Objects.requireNonNull(sku);
        this.title = Objects.requireNonNull(title);
        this.price = Objects.requireNonNull(price);
        this.tags = Set.copyOf(tags);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(sku, product.sku) && Objects.equals(title, product.title) && Objects.equals(price, product.price) && Objects.equals(tags, product.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sku, title, price, tags);
    }

    @Override
    public String toString() {
        return "Product{" +
                "sku='" + sku + '\'' +
                ", title='" + title + '\'' +
                ", price=" + price +
                ", tags=" + tags +
                '}';
    }
}
