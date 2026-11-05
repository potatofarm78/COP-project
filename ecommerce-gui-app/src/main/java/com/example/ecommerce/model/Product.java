package com.example.ecommerce.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Immutable catalogue product with an exact USD price, stored to two decimal places.
 */
public record Product(String id, String name, BigDecimal priceUsd) {

    public Product {
        id = Objects.requireNonNull(id, "Product id is required.").strip();
        name = Objects.requireNonNull(name, "Product name is required.").strip();
        Objects.requireNonNull(priceUsd, "Product price is required.");

        if (id.isEmpty() || name.isEmpty()) {
            throw new IllegalArgumentException("Product id and name must not be blank.");
        }
        if (priceUsd.signum() < 0 || priceUsd.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("Product price must be non-negative with at most two decimal places.");
        }

        priceUsd = priceUsd.setScale(2);
    }
}
