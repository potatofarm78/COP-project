package com.example.ecommerce.checkout;

import com.example.currencyconverter.CurrencyConverterImpl;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.model.ShoppingCart;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PurchaseServiceTest {

    @Test
    void returnsTheExportedReceiptAndPdfLocation() throws IOException {
        AtomicReference<CheckoutReceipt> exported = new AtomicReference<>();
        PurchaseService service = new PurchaseService(new CurrencyConverterImpl(), receipt -> {
            exported.set(receipt);
            return Path.of("receipt.pdf");
        });

        CheckoutReceipt receipt = service.prepareReceipt(cart(), "EUR");
        PurchaseResult result = service.completeCheckout(receipt);

        assertEquals(Path.of("receipt.pdf"), result.receiptPath());
        assertSame(receipt, exported.get());
        assertSame(receipt, result.receipt());
    }

    @Test
    void aFailedReceiptDownloadKeepsTheCartAvailableForRetry() {
        ShoppingCart cart = cart();
        PurchaseService service = new PurchaseService(new CurrencyConverterImpl(), receipt -> {
            throw new IOException("Output directory is unavailable.");
        });
        CheckoutReceipt receipt = service.prepareReceipt(cart, "USD");

        assertThrows(IOException.class, () -> service.completeCheckout(receipt));
        assertEquals(1, cart.getItemCount());
    }

    private ShoppingCart cart() {
        ShoppingCart cart = new ShoppingCart();
        cart.add(new Product("headphones", "Headphones", new BigDecimal("150.00")));
        return cart;
    }
}
