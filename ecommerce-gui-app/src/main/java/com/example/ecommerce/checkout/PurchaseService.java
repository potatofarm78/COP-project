package com.example.ecommerce.checkout;

import com.example.currencyconverter.CurrencyConverter;
import com.example.ecommerce.model.ShoppingCart;
import com.example.ecommerce.receipt.ReceiptExporter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

/** Coordinates conversion and receipt download without depending on GUI controls. */
public final class PurchaseService {

    private final CheckoutService checkout;
    private final ReceiptExporter exporter;

    public PurchaseService(CurrencyConverter converter, ReceiptExporter exporter) {
        checkout = new CheckoutService(converter);
        this.exporter = Objects.requireNonNull(exporter);
    }

    /** Capture observable cart state on the JavaFX thread before starting background work. */
    public CheckoutReceipt prepareReceipt(ShoppingCart cart, String currency) {
        return checkout.checkout(cart, currency);
    }

    /** Perform file I/O on a worker thread and return the completed receipt download. */
    public PurchaseResult completeCheckout(CheckoutReceipt receipt) throws IOException {
        Path downloaded = exporter.export(receipt);
        return new PurchaseResult(receipt, downloaded);
    }
}
