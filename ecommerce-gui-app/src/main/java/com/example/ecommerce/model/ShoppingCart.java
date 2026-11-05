package com.example.ecommerce.model;

import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Shopping cart with exact decimal totals and observable state for the GUI.
 * All mutations are performed on the JavaFX application thread by StoreView.
 */
public final class ShoppingCart {

    public static final int MAX_QUANTITY = 99;

    // The extractor makes changes to an existing item's quantity observable as list updates.
    private final ObservableList<CartItem> items = FXCollections.observableArrayList(
            item -> new Observable[]{item.quantityProperty()});
    private final ObservableList<CartItem> readOnlyItems = FXCollections.unmodifiableObservableList(items);
    private final ReadOnlyObjectWrapper<BigDecimal> totalUsd =
            new ReadOnlyObjectWrapper<>(new BigDecimal("0.00"));
    private final ReadOnlyIntegerWrapper itemCount = new ReadOnlyIntegerWrapper(0);

    public ShoppingCart() {
        items.addListener((ListChangeListener<CartItem>) change -> recalculateTotals());
    }

    /** Adds one unit; repeated additions merge into the existing product row. */
    public void add(Product product) {
        Objects.requireNonNull(product, "Product is required.");

        for (CartItem item : items) {
            if (item.getProduct().id().equals(product.id())) {
                if (!item.getProduct().equals(product)) {
                    throw new IllegalArgumentException("A different product already uses id: " + product.id());
                }
                updateQuantity(item, item.getQuantity() + 1);
                return;
            }
        }

        items.add(new CartItem(product));
    }

    public void updateQuantity(CartItem item, int quantity) {
        if (!items.contains(item)) {
            throw new IllegalArgumentException("The item does not belong to this shopping cart.");
        }
        if (quantity < 1 || quantity > MAX_QUANTITY) {
            throw new IllegalArgumentException("Quantity must be between 1 and " + MAX_QUANTITY + ".");
        }

        item.setQuantity(quantity);
    }

    public void remove(CartItem item) {
        items.remove(item);
    }

    public void clear() {
        items.clear();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public ObservableList<CartItem> getItems() {
        return readOnlyItems;
    }

    public BigDecimal getTotalUsd() {
        return totalUsd.get();
    }

    public ReadOnlyObjectProperty<BigDecimal> totalUsdProperty() {
        return totalUsd.getReadOnlyProperty();
    }

    public int getItemCount() {
        return itemCount.get();
    }

    public ReadOnlyIntegerProperty itemCountProperty() {
        return itemCount.getReadOnlyProperty();
    }

    private void recalculateTotals() {
        BigDecimal total = new BigDecimal("0.00");
        int count = 0;

        for (CartItem item : items) {
            total = total.add(item.getLineTotalUsd());
            count += item.getQuantity();
        }

        totalUsd.set(total);
        itemCount.set(count);
    }
}
