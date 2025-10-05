package org.smartMart.domain.cart;

public record CartLine(
        String sku,
        Integer qty
) {}
