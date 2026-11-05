package com.example.ecommerce.checkout;

import com.example.currencyconverter.CurrencyConverter;
import com.example.currencyconverter.CurrencyConverterImpl;
import com.example.currencyconverter.exception.InvalidCurrencyException;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.model.ShoppingCart;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CheckoutServiceTest {

    private static final Product LAPTOP = new Product("laptop", "Laptop", new BigDecimal("1200.00"));
    private static final Product HEADPHONES = new Product("headphones", "Headphones", new BigDecimal("150.00"));

    private final CheckoutService service = new CheckoutService(new CurrencyConverterImpl());

    @ParameterizedTest
    @CsvSource({"USD, 2550.00", "EUR, 2346.00", "GBP, 1989.00", "KES, 328950.00"})
    void convertsTheGrandTotalIntoEachSupportedCurrency(String currency, String expected) {
        CheckoutReceipt receipt = service.checkout(populatedCart(), currency);

        assertEquals(new BigDecimal("2550.00"), receipt.totalUsd());
        assertEquals(new BigDecimal(expected), receipt.convertedTotal());
        assertEquals(currency, receipt.localCurrency());
    }

    @Test
    void delegatesExactlyOnceThroughTheInterfaceAndRoundsTheReceipt() {
        AtomicInteger calls = new AtomicInteger();
        CurrencyConverter substitute = (amount, from, to) -> {
            calls.incrementAndGet();
            assertEquals(2550.0, amount);
            assertEquals("USD", from);
            assertEquals("GBP", to);
            return 42.125;
        };

        CheckoutReceipt receipt = new CheckoutService(substitute).checkout(populatedCart(), "GBP");

        assertEquals(1, calls.get());
        assertEquals(new BigDecimal("42.13"), receipt.convertedTotal());
    }

    @Test
    void receiptIsAnImmutableSnapshot() {
        ShoppingCart cart = populatedCart();
        CheckoutReceipt receipt = service.checkout(cart, "EUR");
        cart.updateQuantity(cart.getItems().get(0), 1);
        cart.clear();

        assertEquals(2, receipt.items().size());
        assertEquals(2, receipt.items().get(0).quantity());
        assertEquals(new BigDecimal("2550.00"), receipt.totalUsd());
        assertThrows(UnsupportedOperationException.class, () -> receipt.items().clear());
    }

    @Test
    void rejectsEmptyCartCheckout() {
        assertThrows(IllegalStateException.class, () -> service.checkout(new ShoppingCart(), "USD"));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "JPY"})
    void propagatesCustomInvalidCurrencyErrors(String currency) {
        assertThrows(InvalidCurrencyException.class, () -> service.checkout(populatedCart(), currency));
    }

    @Test
    void receiptIncludesItemsQuantitiesAndBothTotals() {
        String text = service.checkout(populatedCart(), "EUR").toDisplayText();

        assertTrue(text.contains("GLOBALMART RECEIPT"));
        assertTrue(text.contains("Receipt: GM-"));
        assertTrue(text.contains("Purchased:"));
        assertTrue(text.contains("2 x Laptop @ 1,200.00 USD = 2,400.00 USD"));
        assertTrue(text.contains("1 x Headphones @ 150.00 USD = 150.00 USD"));
        assertTrue(text.contains("Total (USD): 2,550.00 USD"));
        assertTrue(text.contains("Total (EUR): 2,346.00 EUR"));
    }

    private ShoppingCart populatedCart() {
        ShoppingCart cart = new ShoppingCart();
        cart.add(LAPTOP);
        cart.add(LAPTOP);
        cart.add(HEADPHONES);
        return cart;
    }
}
