package com.example.ecommerce.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShoppingCartTest {

    private static final Product LAPTOP = new Product("laptop", "Laptop", new BigDecimal("1200.00"));
    private static final Product HEADPHONES = new Product("headphones", "Headphones", new BigDecimal("150.00"));

    private final ShoppingCart cart = new ShoppingCart();

    @Test
    void mergesRepeatedAddsAndCountsAllUnits() {
        cart.add(LAPTOP);
        cart.add(LAPTOP);
        cart.add(HEADPHONES);

        assertEquals(2, cart.getItems().size());
        assertEquals(2, cart.getItems().get(0).getQuantity());
        assertEquals(3, cart.getItemCount());
        assertEquals(new BigDecimal("2550.00"), cart.getTotalUsd());
    }

    @Test
    void quantityChangesUpdateLineTotalsAndCartTotals() {
        cart.add(LAPTOP);
        CartItem item = cart.getItems().get(0);
        cart.updateQuantity(item, 3);

        assertEquals(new BigDecimal("3600.00"), item.lineTotalUsdProperty().get());
        assertEquals(new BigDecimal("3600.00"), cart.getTotalUsd());
        assertEquals(3, cart.getItemCount());
    }

    @Test
    void sumsDecimalPricesExactly() {
        cart.add(new Product("first", "First item", new BigDecimal("0.10")));
        cart.add(new Product("second", "Second item", new BigDecimal("0.20")));

        assertEquals(new BigDecimal("0.30"), cart.getTotalUsd());
    }

    @Test
    void removalAndClearResetTotals() {
        cart.add(LAPTOP);
        cart.add(HEADPHONES);
        cart.remove(cart.getItems().get(0));

        assertEquals(new BigDecimal("150.00"), cart.getTotalUsd());
        assertEquals(1, cart.getItemCount());

        cart.clear();
        assertTrue(cart.isEmpty());
        assertEquals(new BigDecimal("0.00"), cart.getTotalUsd());
        assertEquals(0, cart.getItemCount());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 100})
    void rejectsOutOfRangeQuantitiesWithoutChangingTheCart(int quantity) {
        cart.add(LAPTOP);
        CartItem item = cart.getItems().get(0);

        assertThrows(IllegalArgumentException.class, () -> cart.updateQuantity(item, quantity));
        assertEquals(1, item.getQuantity());
        assertEquals(new BigDecimal("1200.00"), cart.getTotalUsd());
    }

    @Test
    void repeatedAddsRespectTheQuantityLimit() {
        cart.add(LAPTOP);
        cart.updateQuantity(cart.getItems().get(0), ShoppingCart.MAX_QUANTITY);

        assertThrows(IllegalArgumentException.class, () -> cart.add(LAPTOP));
        assertEquals(99, cart.getItemCount());
        assertEquals(new BigDecimal("118800.00"), cart.getTotalUsd());
    }

    @Test
    void rejectsItemsFromAnotherCart() {
        ShoppingCart anotherCart = new ShoppingCart();
        anotherCart.add(LAPTOP);

        assertThrows(IllegalArgumentException.class,
                () -> cart.updateQuantity(anotherCart.getItems().get(0), 2));
        assertTrue(cart.isEmpty());
    }

    @Test
    void protectsTheObservableListFromExternalEdits() {
        cart.add(LAPTOP);

        assertThrows(UnsupportedOperationException.class, () -> cart.getItems().clear());
        assertEquals(1, cart.getItems().size());
    }

    @Test
    void rejectsConflictingProductIds() {
        cart.add(LAPTOP);
        Product conflictingProduct = new Product("laptop", "Other Laptop", new BigDecimal("999.00"));

        assertThrows(IllegalArgumentException.class, () -> cart.add(conflictingProduct));
        assertEquals(new BigDecimal("1200.00"), cart.getTotalUsd());
    }
}
