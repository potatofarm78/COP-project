package com.example.ecommerce.model;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;

import java.math.BigDecimal;

/**
 * One product and its quantity. Read-only properties allow JavaFX tables to observe changes.
 */
public final class CartItem {

    private final Product product;
    private final ReadOnlyIntegerWrapper quantity = new ReadOnlyIntegerWrapper(1);
    private final ReadOnlyObjectWrapper<BigDecimal> lineTotalUsd = new ReadOnlyObjectWrapper<>();

    CartItem(Product product) {
        this.product = product;
        lineTotalUsd.bind(Bindings.createObjectBinding(this::getLineTotalUsd, quantity));
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity.get();
    }

    public ReadOnlyIntegerProperty quantityProperty() {
        return quantity.getReadOnlyProperty();
    }

    public BigDecimal getLineTotalUsd() {
        return product.priceUsd().multiply(BigDecimal.valueOf(getQuantity()));
    }

    public ReadOnlyObjectProperty<BigDecimal> lineTotalUsdProperty() {
        return lineTotalUsd.getReadOnlyProperty();
    }

    // ShoppingCart is responsible for validating quantities before changing them.
    void setQuantity(int quantity) {
        this.quantity.set(quantity);
    }
}
